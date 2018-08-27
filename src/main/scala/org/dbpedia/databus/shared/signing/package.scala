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

import org.dbpedia.databus.shared.helpers.conversions.TapableW

import better.files.File
import cats.effect.IO
import com.google.common.hash.{Hasher, Hashing}
import com.typesafe.scalalogging.LazyLogging
import fs2.io

import java.io.InputStream
import java.security.{PrivateKey, PublicKey, Signature}
import java.util.concurrent.atomic.AtomicInteger

package object signing extends LazyLogging {

  val bufferSizeCrypt = 32 * 1024

  var bufferSizeHash = 32 * 1024

  def hashFile(file: File, hasher: Hasher) = {

    val chunkCounter = new AtomicInteger()

    io.file.readAll[IO](file.path, bufferSizeHash).chunks
      .compile.fold(hasher)({ case (hasher, byteChunk) => {
      chunkCounter.incrementAndGet()
      hasher.putBytes(byteChunk.toArray)
    }
    }).unsafeRunSync().hash()
  }

  def sha256Hash(file: File) = hashFile(file, Hashing.sha256().newHasher())

  def signFile(privateKey: PrivateKey, file: File, algorithmName: String = "SHA1withRSA",
    bufferSize: Int = bufferSizeCrypt): Array[Byte] = {

    sign(privateKey, io.file.readAll[IO](file.path, bufferSize), algorithmName)
  }

  def signInputStream(privateKey: PrivateKey, data: InputStream, algorithmName: String = "SHA1withRSA",
    bufferSize: Int = bufferSizeCrypt): Array[Byte] = {

    sign(privateKey, io.readInputStream(IO(data), bufferSize), algorithmName)
  }

  def sign(privateKey: PrivateKey, dataStream: fs2.Stream[IO, Byte],
    algorithmName: String = "SHA1withRSA"): Array[Byte] = {

    def signatureForSign = Signature.getInstance(algorithmName) tap { _ initSign privateKey }

    dataStream.chunks.compile.fold(signatureForSign)({

      case (sig, bytes) => sig tap { _.update(bytes.toArray) }
    }).unsafeRunSync().sign()
  }

  def verifyFile(publicKey: PublicKey, signature: Array[Byte], file: File, algorithmName: String = "SHA1withRSA",
    bufferSize: Int = bufferSizeCrypt): Boolean = {

    verify(publicKey, signature, io.file.readAll[IO](file.path, bufferSize), algorithmName)
  }

  def verifyInputStream(publicKey: PublicKey, signature: Array[Byte], data: InputStream,
    algorithmName: String = "SHA1withRSA", bufferSize: Int = bufferSizeCrypt): Boolean = {

    verify(publicKey, signature, io.readInputStream(IO(data), bufferSize), algorithmName)
  }


  def verify(publicKey: PublicKey, signature: Array[Byte] ,dataStream: fs2.Stream[IO, Byte],
    algorithmName: String = "SHA1withRSA"): Boolean = {

    def signatureForVerify = Signature.getInstance(algorithmName) tap { _ initVerify publicKey }

    dataStream.chunks.compile.fold(signatureForVerify)({

      case (sig, bytes) => sig tap { _.update(bytes.toArray) }
    }).unsafeRunSync().verify(signature)
  }
}
