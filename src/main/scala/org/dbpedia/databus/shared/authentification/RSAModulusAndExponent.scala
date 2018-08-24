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

import org.dbpedia.databus.shared.rdf.vocab

import org.apache.jena.rdf.model.{Model, RDFNode}
import org.apache.maven.plugin.logging.Log
import resource.managed

import scala.collection.JavaConverters._

case class RSAModulusAndExponent(modulus: BigInt, exponent: BigInt) {

  def modulusHex = modulus.toString(16).toUpperCase

  def matchAgainstWebId(webIdModel: Model, maintainerIRI: String, mavenLog: Option[Log] = None): Option[RDFNode] = {

    val w3cCert = vocab.W3CCert.inModel(webIdModel)

    val maintainerRes = webIdModel.getResource(maintainerIRI)

    managed(webIdModel.listObjectsOfProperty(maintainerRes, w3cCert.key)) apply { nodeIter =>

      nodeIter.asScala find { certKey =>

        val exponentOpt = Option(certKey.asResource().getProperty(w3cCert.exponent))
          .map(stmt => BigInt(stmt.getObject.asLiteral().getLexicalForm))


        val modulusOpt = Option(certKey.asResource().getProperty(w3cCert.modulus))
          .map(stmt => BigInt(stmt.getObject.asLiteral.getLexicalForm, 16))

        (exponentOpt, modulusOpt) match {

          case (Some(exponent), Some(modulus)) => {

            val modulusExponentFromWebId = RSAModulusAndExponent(modulus, exponent)


            mavenLog foreach { log =>
              if(modulusExponentFromWebId == this) {
                log.info("Key with matching exponent and modulus found in WebID:\n" +
                  modulusExponentFromWebId.shortenedDescription)
              } else {
                log.info("Ingoring key with differing exponent and modulus found in WebID:\n" +
                  modulusExponentFromWebId.shortenedDescription)
              }
            }

            modulusExponentFromWebId == this
          }

          case _ => {

            mavenLog foreach {
              _ warn "Malformed http://www.w3.org/ns/auth/cert#key resource in WebID"
            }

            false
          }
        }
      }
    }
  }

  def shortenedDescription =
    s"""
       |modulus:  ${modulusHex.take(30)}...
       |exponent: $exponent
       """.stripMargin
}
