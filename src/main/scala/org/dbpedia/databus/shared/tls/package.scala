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

import org.dbpedia.databus.shared.helpers.resourceAsStream

import javax.net.ssl.{KeyManagerFactory, SSLContext}
import resource.ManagedResource
import scalaj.http.{BaseHttp, HttpOptions}

import java.io.InputStream
import java.security.KeyStore

package object tls {

  def pkcsClientCertSslContext(pkcs12BundleResourceName: String): SSLContext = {

    pkcsClientCertSslContext(resourceAsStream(pkcs12BundleResourceName))
  }

  def pkcsClientCertSslContext(pkcs12Data: ManagedResource[InputStream]): SSLContext = {

    pkcs12Data apply { bundleStream =>

      val password = ""

      val ks = KeyStore.getInstance("PKCS12")
      ks.load(bundleStream, password.toCharArray)

      val kmf = KeyManagerFactory.getInstance("SunX509")
      kmf.init(ks, password.toCharArray)

      val sslContext = SSLContext.getInstance("TLS")
      sslContext.init(kmf.getKeyManagers, null, null)
      sslContext
    }
  }

  def scalajHttpWithClientCert(pkcs12Data: ManagedResource[InputStream]) = {

    val sslContext = pkcsClientCertSslContext(pkcs12Data)

    val httpOptions = Seq(
      HttpOptions.connTimeout(1000),
      HttpOptions.readTimeout(30000),
      HttpOptions.followRedirects(false),
      HttpOptions.sslSocketFactory(sslContext.getSocketFactory),
    )

    new BaseHttp(options = httpOptions)
  }
}
