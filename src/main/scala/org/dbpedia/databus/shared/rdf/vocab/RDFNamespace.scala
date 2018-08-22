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
package org.dbpedia.databus.shared.rdf.vocab

import org.apache.jena.rdf.model.{Model, Property, Resource, ResourceFactory}

import scala.language.dynamics

trait RDFNamespaceLike {

  def namespace: String

  def resource(suffix: String): Resource

  def property(suffix: String): Property

  lazy val res = new Dynamic {

    def selectDynamic(suffix: String) = resource(suffix)
  }

  lazy val prop = new Dynamic {

    def selectDynamic(suffix: String) = property(suffix)
  }
}

trait UnboundRDFNamespace extends RDFNamespaceLike {

  override def resource(suffix: String): Resource = ResourceFactory.createResource(namespace + suffix)

  override def property(suffix: String): Property = ResourceFactory.createProperty(namespace + suffix)
}

trait RDFNamespaceInModel extends RDFNamespaceLike {

  def model: Model

  override def resource(suffix: String): Resource = model.createResource(namespace + suffix)

  override def property(suffix: String): Property = model.createProperty(namespace + suffix)
}

trait RDFNamespaceVocab extends RDFNamespaceLike

trait RDFNamespace extends UnboundRDFNamespace with RDFNamespaceVocab {

  def inModel(contextModel: Model): RDFNamespaceInModel

}
