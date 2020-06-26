/*-
 * #%L
 * DBpedia Databus Shared Library
 * %%
 * Copyright (C) 2018 - 2019 Sebastian Hellmann (on behalf of the DBpedia Association)
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


import java.net.URL

import org.apache.jena.rdf.model.{Model, ModelFactory, Resource}
import org.apache.jena.riot.RDFLanguages.TURTLE
import org.dbpedia.databus.shared.rdf.vocab.foaf
import org.dbpedia.databus.shared.rdf.conversions._
import org.dbpedia.databus.shared.helpers.conversions._

object AccountHelpers {

  //retrieving all User Accounts
  @volatile
  var registeredAccounts: Model = update


  def update: Model = {
    ModelFactory.createDefaultModel.tap {
      accountsModel =>
        //accountsModel.read("https://raw.githubusercontent.com/dbpedia/accounts/master/accounts.ttl", TURTLE.getName)
        accountsModel.read("https://databus.dbpedia.org/system/api/accounts", TURTLE.getName)
    }
  }

  private def getAccountOptionInternal(publisher: String) = {
    lazy val accountOption = {
      implicit val userAccounts: Model = registeredAccounts
      Option(publisher.asIRI.getProperty(foaf.account)).map(_.getObject.asResource)

    }
    accountOption
  }

  def getAccountOption(publisher: URL): Option[Resource] = {
    getAccountOption(publisher.toString)
  }

  /**
    * check for user account
    * NOTE: refreshes account list, if no account found
    *
    * @param publisher
    */
  def getAccountOption(publisher: String): Option[Resource] = {
    getAccountOptionInternal(publisher) match {
      case Some(resource) => Some(resource)
      case None => {
        //try updateing
        registeredAccounts = update
        getAccountOptionInternal(publisher) match {
          case Some(resource) => Some(resource)
          case None => None
        }
      }
    }
  }
}
