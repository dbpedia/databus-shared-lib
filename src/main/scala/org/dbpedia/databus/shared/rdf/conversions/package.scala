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

import org.dbpedia.databus.shared.errors

import org.apache.jena.datatypes.RDFDatatype
import org.apache.jena.iri.IRIFactory
import org.apache.jena.rdf.model._
import org.apache.jena.vocabulary.RDF

import scala.collection.JavaConverters._
import scala.util.{Failure, Success, Try}

package object conversions {

  implicit class RDFResourceW(val res: Resource) extends AnyVal {

    def getRequiredFunctionalProperty(prop: Property)
      (implicit errorGen: String => Throwable) = {

      res.listProperties(prop).asScala.toList match {

        case singleStmt :: Nil => ObjectInStatement(singleStmt)

        case Nil => throw errorGen(s"no ${prop.getURI} value for $res")

        case _ => throw errorGen(s"several ${prop.getURI} values for $res")
      }
    }

    def hasType(rdfType: Resource) = {

      res.listProperties(RDF.`type`).asScala.exists(_.getObject == rdfType)
    }
  }

  implicit class StringJenaW(val str: String) extends AnyVal {

    def asIRI(implicit model: Model) = model.createResource(str)

    def asAbsoluteIRI(implicit model: Model) = {

      Try(IRIFactory.iriImplementation().construct(str)).flatMap({

        case absolute if absolute.isAbsolute => Success(model.createResource(str))

        case notAbsolute => Failure(errors.unexpectedIriFormat(
          s"'$notAbsolute' does not denote an absolute IRI"))
      }).get
    }

    def asPlainLiteral(implicit model: Model) = {
      model.createLiteral(str)
    }

    def asLangLiteral(langTag: String)(implicit model: Model) = {
      model.createLiteral(str, langTag)
    }

    def asTypedLiteral(dtype: RDFDatatype)(implicit model: Model) = {
      model.createTypedLiteral(str, dtype)
    }
  }

  trait NodeInStatement[N <: RDFNode] {

    def node: N

    def statement: Statement
  }

  case class SubjectInStatement(statement: Statement) extends NodeInStatement[Resource]
    with ResourceCoercions {

    def node = statement.getSubject
  }

  case class ObjectInStatement(statement: Statement) extends NodeInStatement[RDFNode]
    with ResourceCoercions {

    def node = statement.getObject
  }

  trait ResourceCoercions { this: NodeInStatement[_ <: RDFNode] =>

    def coerceUriResource = if(node.isURIResource) Success(node.asResource()) else {
      Failure(errors.unexpectedRdfFormat(
        s"$node is not an URI-resource:\n$statement"))
    }

    def coerceLiteral = Try(node.asLiteral()).recoverWith {

      case rre: ResourceRequiredException => Failure(errors.unexpectedRdfFormat(
        s"$node is not a literal:\n$statement"))

      case ex => Failure(ex)
    }
  }
}
