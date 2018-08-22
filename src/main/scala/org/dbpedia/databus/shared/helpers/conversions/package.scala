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
package org.dbpedia.databus.shared.helpers

package object conversions {

  /**
    * Mimicks the `Object#tap` method from Ruby.
    * All Scala values can be implicitly converted to offer the `tap` method,
    * that receives a thunk. The tunk in turn will be called with the `this`
    * reference of the decorated value and will return the `this values` thereafter
    * to allow for method chaining.
    *
    * One possible use case is to get mandatory initialisation of a freshly instatiated
    * mutable done before other parts of the program will get access to it's reference:
    *
    * {{{
    *   val properlyInitialised = new Mutable().tap { mutable =>
    *     mutable.setImportantParameter(42)
    *     mutable.init
    *   }
    * }}}
    *
    *
    * @param anyVal reference to the decorated object (via implicit conversion)
    * @tparam T type of the decorate object
    * @see [[http://ruby-doc.org/core-2.5.1/Object.html#method-i-tap]]
    */
  implicit class TapableW[T](val anyVal: T) extends AnyVal {

    def tap(work: T => Unit) = {
      work apply anyVal
      anyVal
    }
  }
}
