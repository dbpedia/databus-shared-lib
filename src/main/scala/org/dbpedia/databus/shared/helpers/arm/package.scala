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

import better.files.File
import resource.Resource

import java.io.IOException

package object arm {

  implicit def deleteFileAfterWorkResource = new Resource[File] {

    override def close(r: File): Unit = try r.delete() catch {

      case ioe: IOException => throw new RuntimeException(s"Error while cleaning up file ${r.pathAsString}", ioe)
    }
  }

  implicit def noopFileResource = new Resource[File] {

    override def close(r: File): Unit = ()
  }
}
