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

  object global {

    def dataid = DataId

    def dcat = Dcat

    def dcterms = DCTerms

    def foaf = Foaf

    def prov = ProvO

    def schemaOrg = SchemaOrg

    def w3cCert = W3CCert
  }

  def dataid(implicit model: Model) = DataId.inModel(model)

  def dcat(implicit model: Model) = Dcat.inModel(model)

  def dcterms(implicit model: Model) = DCTerms.inModel(model)

  def foaf(implicit model: Model) = Foaf.inModel(model)

  def prov(implicit model: Model) = ProvO.inModel(model)

  def schemaOrg(implicit model: Model) = SchemaOrg.inModel(model)

  def w3cCert(implicit model: Model) = W3CCert.inModel(model)

  trait DataIdVocab extends RDFNamespaceVocab {

    def namespace = "http://dataid.dbpedia.org/ns/core#"

    lazy val DataId = resource("DataId")

    lazy val Dataset = resource("Dataset")

    lazy val SingleFile = resource("SingleFile")

    lazy val Artifact = resource("Artifact")

    lazy val Group = resource("Group")

    lazy val Version = resource("Version")

    lazy val artifact = property("artifact")

    lazy val version = property("version")

    lazy val changelog = property("changelog")

    lazy val associatedAgent = property("associatedAgent")

    lazy val maintainer = property("maintainer")

    lazy val groupId = property("groupid")

    lazy val compression = property("compression")

    lazy val isDistributionOf = property("isDistributionOf")

    lazy val mimetype = property("mimetype")

    lazy val preview = property("preview")

    lazy val sha256sum = property("sha256sum")

    lazy val signature = property("signature")

    lazy val uncompressedByteSize = property("uncompressedByteSize")

    lazy val duplicates = property("duplicates")

    lazy val nonEmptyLines = property("nonEmptyLines")

    lazy val sorted = property("sorted")
  }

  trait DcatVocab extends RDFNamespaceVocab {

    def namespace: String = "http://www.w3.org/ns/dcat#"

    lazy val byteSize = property("byteSize")

    lazy val distribution = property("distribution")

    lazy val downloadURL = property("downloadURL")

    lazy val mediaType = property("mediaType")
  }

  trait DCTermsVocab extends RDFNamespaceVocab {

    def namespace: String = "http://purl.org/dc/terms/"

    lazy val conformsTo = property("conformsTo")

    lazy val description = property("description")

    lazy val hasVersion = property("hasVersion")

    lazy val identifier = property("identifier")

    lazy val issued = property("issued")

    lazy val license = property("license")

    lazy val modified = property("modified")

    lazy val publisher = property("publisher")

    lazy val title = property("title")
  }

  trait FoafVocab extends RDFNamespaceVocab {

    override def namespace: String = "http://xmlns.com/foaf/0.1/"

    lazy val account = property("account")
  }

  trait ProvOVocab extends RDFNamespaceVocab {

    override def namespace: String = "http://www.w3.org/ns/prov#"

    lazy val Activity = resource("Activity")

    lazy val Agent = resource("Agent")

    lazy val Entity = resource("Entity")

    lazy val wasDerivedFrom = property("wasDerivedFrom")

    lazy val wasInformedBy = property("wasInformedBy")

    lazy val used = property("used")
  }

  trait SchemaOrgVocab extends RDFNamespaceVocab {

    override def namespace: String = "http://schema.org/version"

    lazy val version = property("version")
  }

  trait W3CCertVocab extends RDFNamespaceVocab {

    def namespace: String = "http://www.w3.org/ns/auth/cert#"

    lazy val RSAPublicKey = resource("RSAPublicKey")

    lazy val exponent = property("exponent")

    lazy val key = property("key")

    lazy val modulus = property("modulus")
  }

  object DataId extends RDFNamespace with DataIdVocab {

    override def inModel(contextModel: Model) = {

      new RDFNamespaceInModel with DataIdVocab {

        override def model: Model = contextModel
      }
    }
  }

  object Dcat extends RDFNamespace with DcatVocab {

    override def inModel(contextModel: Model) = {

      new RDFNamespaceInModel with DcatVocab {

        override def model: Model = contextModel
      }
    }
  }

  object DCTerms extends RDFNamespace with DCTermsVocab {

    override def inModel(contextModel: Model) = {

      new RDFNamespaceInModel with DCTermsVocab {

        override def model: Model = contextModel
      }
    }
  }

  object Foaf extends RDFNamespace with FoafVocab {

    override def inModel(contextModel: Model) = {

      new RDFNamespaceInModel with FoafVocab {

        override def model: Model = contextModel
      }
    }
  }

  object ProvO extends RDFNamespace with ProvOVocab {

    override def inModel(contextModel: Model) = {

      new RDFNamespaceInModel with ProvOVocab {

        override def model: Model = contextModel
      }
    }
  }

  object SchemaOrg extends RDFNamespace with SchemaOrgVocab {

    override def inModel(contextModel: Model) = {

      new RDFNamespaceInModel with SchemaOrgVocab {

        override def model: Model = contextModel
      }
    }
  }

  object W3CCert extends RDFNamespace with W3CCertVocab {

    override def inModel(contextModel: Model) = {

      new RDFNamespaceInModel with W3CCertVocab {

        override def model: Model = contextModel
      }
    }
  }

}
