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

import org.dbpedia.databus.shared.helpers._

import org.scalatest.{FlatSpec, Matchers}

import scala.collection.JavaConverters._

class PKCS12FileTest extends FlatSpec with Matchers {

  "A PKCS12File instance created from the test identity .p12 file" should
    "provide the contained single public key pair" in {

    resoucreAsFile("test-id.p12") apply { p12File =>

      val pkcs12 = PKCS12File(p12File)

      pkcs12.certificates should have size (1)

      pkcs12.rsaKeyPairs should have size (1)

      val keyPair = pkcs12.rsaKeyPairs.head

      keyPair.publicModulusAndExponent.modulusHex should startWith("A7AB0")

      keyPair.publicModulusAndExponent.exponent should equal(65537)

      pkcs12.findMatchingKeyPair(keyPair.publicModulusAndExponent).isDefined should be(true)

      pkcs12.uriAlternativeNames.flatten should have size (1)

      pkcs12.uriAlternativeNames.flatten.head should endWith ("test-id.ttl#this")
    }
  }
}
