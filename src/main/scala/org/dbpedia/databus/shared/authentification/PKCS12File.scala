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
package org.dbpedia.databus.shared.authentification

import org.dbpedia.databus.shared.helpers.conversions.TapableW

import better.files.File
import com.typesafe.scalalogging.LazyLogging
import javax.net.ssl.{KeyManagerFactory, X509KeyManager}
import resource._

import scalaz._
import Scalaz._

import java.security.KeyStore
import java.security.interfaces.{RSAPrivateKey, RSAPublicKey}


/**
  * Assumptions:
  *   - archive of one X.509 certificate together with matching private key
  *   - RSA key pair
  *
  * @param file
  * @param password
  */

case class PKCS12File(file: File, password: String = "") extends LazyLogging {

  lazy val keyStore = managed(file.newInputStream) apply { is =>

    KeyStore.getInstance("PKCS12").tap {

      _.load(is, password.toCharArray)
    }
  }

  lazy val keyManagerArray = {

    val kmf = KeyManagerFactory.getInstance("SunX509").tap {
      _.init(keyStore, password.toCharArray)
    }

    kmf.getKeyManagers
  }

  lazy val keyManagers = keyManagerArray.toList

  lazy val rsaKeyPairs = {

    val nestedContainers = keyManagers collect { case x509: X509KeyManager =>

      val aliases = x509.getClientAliases("RSA", null)

      aliases map { alias =>

        def publicKey = x509.getCertificateChain(alias).head.getPublicKey match {

          case rsa: RSAPublicKey => rsa.some

          case _ => {
            logger.warn(s"encountered non-RSA public key in PKCS12 file ${file.pathAsString}")
            None
          }
        }

        def privateKey = x509.getPrivateKey(alias) match {

          case rsa: RSAPrivateKey => rsa.some

          case _ => {
            logger.warn(s"encountered non-RSA private key in PKCS12 file ${file.pathAsString}")
            None
          }
        }

        (publicKey |@| privateKey) apply (RSAKeyPair)
      }
    }

    nestedContainers.flatten.flatten.toList
  }

  def findMatchingKeyPair(modExp: RSAModulusAndExponent) = {

    implicit def eqModExp = Equal.equalA[RSAModulusAndExponent]

    rsaKeyPairs.find(_.publicModulusAndExponent === modExp)
  }
}
