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
