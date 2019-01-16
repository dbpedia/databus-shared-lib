/*-
 * #%L
 * databus-shared-lib
 * %%
 * Copyright (C) 2018 Sebastian Hellmann (on behalf of the DBpedia Association)
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */
package org.dbpedia.databus.shared

import org.dbpedia.databus.shared.authentification.{PKCS12File, RSAKeyPair}

import better.files.File
import com.typesafe.scalalogging.LazyLogging
import resource._
import scalaj.http.{HttpResponse, MultiPart}

import java.io.{ByteArrayInputStream, InputStream}
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

object DataIdUpload extends LazyLogging {

  def upload(uploadEndpointIRI: String, dataIdFile: File, pkcs12File: File, pkcs12Password: String,
    dataIdTargetLocation: String, allowOverwrite: Boolean, datasetIdentifier:String ): HttpResponse[String] = {

    upload(uploadEndpointIRI, managed(dataIdFile.newInputStream), managed(pkcs12File.newInputStream),
      pkcs12File.pathAsString, pkcs12Password, dataIdTargetLocation, allowOverwrite, datasetIdentifier)
  }

  def upload(uploadEndpointIRI: String, dataIdBytes: Array[Byte], pkcs12File: File, pkcs12Password: String,
    dataIdTargetLocation: String, allowOverwrite: Boolean, datasetIdentifier:String): HttpResponse[String] = {

    upload(uploadEndpointIRI, managed(new ByteArrayInputStream(dataIdBytes)), managed(pkcs12File.newInputStream),
      pkcs12File.pathAsString, pkcs12Password, dataIdTargetLocation, allowOverwrite,datasetIdentifier)
  }

  def upload(uploadEndpointIRI: String, dataIdStreamOpener: => ManagedResource[InputStream],
    pkcs12StreamOpener: => ManagedResource[InputStream], pkcs12SourceDesc: String, pkcs12Password: String,
    dataIdTargetLocation: String, allowOverwrite: Boolean, datasetIdentifier:String) = {

    val dataIdSize = dataIdStreamOpener acquireAndGet { is =>

      Stream.continually(is.read()).takeWhile(_ != -1).size
    }

    (dataIdStreamOpener and dataIdStreamOpener) apply { case (dataIdForSend, dataIdForSign) =>

        val sslContext = tls.pkcs12ClientCertSslContext(pkcs12StreamOpener, pkcs12Password)

        val pkcs12 = PKCS12File.fromStream(pkcs12StreamOpener, pkcs12SourceDesc, pkcs12Password)

        val RSAKeyPair(publicKey, privateKey) = pkcs12.rsaKeyPairs.head

        def dataIdPart = MultiPart(UploadPartNames.dataId, "dataid.ttl", "text/turtle", dataIdForSend, dataIdSize,
          bytesWritten => logger.debug(s"$bytesWritten bytes written for $dataIdTargetLocation"))

        def signaturePart = MultiPart(UploadPartNames.dataIdSignature, "dataid.ttl.sig", "application/pkcs7-signature",
          signing.signInputStream(privateKey, dataIdForSign))

        val params = Map(
          UploadParams.dataIdLocation -> dataIdTargetLocation,
          UploadParams.allowOverwrite -> true.toString,
          UploadParams.datasetIdentifier -> datasetIdentifier
        )

        def encodedParamsQueryString = {

          def encode: String => String = URLEncoder.encode(_, StandardCharsets.UTF_8.name())

          params.map({ case (k, v) => s"$k=${encode(v)}" }).mkString("&")
        }

        def paramsPart = MultiPart(UploadPartNames.uploadParams, "dataid.params", "application/x-www-form-urlencoded",
          encodedParamsQueryString)

        val allowUnsafeSSL = uploadEndpointIRI.contains("localhost")

        val sslHttp = tls.scalajHttpWithClientCert(pkcs12StreamOpener, pkcs12Password, allowUnsafeSSL)

        sslHttp(uploadEndpointIRI).postMulti(dataIdPart, signaturePart, paramsPart).asString
    }
  }

  lazy val expectedPartsForUpload = {

    import UploadPartNames._

    Set(dataId, dataIdSignature, uploadParams)
  }

  object UploadPartNames {

    val (dataId, dataIdSignature, uploadParams) = ("dataid", "dataid-signature", "upload-params")
  }

  object UploadParams {

    val (dataIdLocation, allowOverwrite) = ("DataIdLocation", "AllowOverwrite")

    val (datasetIdentifier, dataIdVersion) = ("DatasetIdentifier", "DataIdVersion")
  }
}
