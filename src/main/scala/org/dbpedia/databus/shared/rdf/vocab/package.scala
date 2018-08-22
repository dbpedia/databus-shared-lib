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
package org.dbpedia.databus.shared.rdf

import org.apache.jena.rdf.model.Model

package object vocab {

  def dataid = DataId

  def dcat = Dcat

  trait DataIdVocab extends RDFNamespaceVocab {

    def namespace = "http://dataid.dbpedia.org/ns/core#"

    lazy val Dataset = resource("Dataset")

    lazy val SingleFile = resource("SingleFile")

    lazy val associatedAgent = property("associatedAgent")

    lazy val compression = property("compression")

    lazy val isDistributionOf = property("isDistributionOf")

    lazy val mimetype = property("mimetype")
    
    lazy val preview = property("preview")

    lazy val sha256sum = property("sha256sum")

    lazy val signature = property("signature")

  }

  trait DcatVocab extends RDFNamespaceVocab {

    lazy val byteSize = property("byteSize")

    lazy val distribution = property("distribution")

    lazy val downloadURL = property("downloadURL")

    lazy val mediaType = property("mediaType")
  }

  object DataId extends RDFNamespace with DataIdVocab {

    override def namespace: String = "http://dataid.dbpedia.org/ns/core#"


    override def inModel(contextModel: Model) = {

      new RDFNamespaceInModel with DataIdVocab {

        override def model: Model = contextModel

        override def namespace: String = DataId.namespace
      }
    }
  }

  object Dcat extends RDFNamespace with DcatVocab {

    override def namespace: String = "http://www.w3.org/ns/dcat#"


    override def inModel(contextModel: Model) = {

      new RDFNamespaceInModel with DcatVocab {

        override def model: Model = contextModel

        override def namespace: String = Dcat.namespace
      }
    }
  }
}
