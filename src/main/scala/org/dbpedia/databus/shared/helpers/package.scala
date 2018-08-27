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
package org.dbpedia.databus.shared

import org.dbpedia.databus.shared.helpers.conversions.TapableW

import better.files._
import cats.effect.IO
import fs2.io
import resource.managed



package object helpers {

  def contextClassLoader = Thread.currentThread().getContextClassLoader()

  /**
    * Looks in the classpath for the named resource and return an ARM-wrapper for an InputStream to read from it.
    *
    * @param name resource name
    * @param classLoader class loader to use (by default: the context class loader of the current thread)
    * @return
    */

  def resourceAsStream(name: String, classLoader: ClassLoader = contextClassLoader) = {

    managed(classLoader.getResourceAsStream(name))
  }


  /**
    * Looks in the classpath for the named resource. If it is present as regular file, it is returned in an
    * ARM-wrapper that does nothing (no-op). If the resource is found in a form different from a regular file
    * (e.g. as entry of a JAR archive), a new temporary file is created and the content of the resource is
    * streamed into the (regular) temporary file to create a copy of it. This copy is returned with an
    * ARM-wrapper that will delete the temporary file after it's use.
    *
    * @param name resource name
    * @param classLoader class loader to use (by default: the context class loader of the current thread)
    * @return
    */
  def resoucreAsFile(name:String, classLoader: ClassLoader = contextClassLoader) = {

    File(classLoader.getResource(name)) match {

      case regularFile if regularFile.isRegularFile => {

        import org.dbpedia.databus.shared.helpers.arm.noopFileResource

        managed(regularFile)
      }

      case other => {

        def suffixFromExt = other.extension(true).getOrElse("")

        def tmpCopy = File.newTemporaryFile(s"resource-copy-$name", suffixFromExt).tap { sink =>

          resourceAsStream(name, classLoader) apply { is =>

            io.readInputStream[IO](IO(is), 32 * 1024)
              .through(io.file.writeAll(sink.path)).compile.drain.unsafeRunSync()
          }
        }

        import org.dbpedia.databus.shared.helpers.arm.deleteFileAfterWorkResource

        managed(tmpCopy)
      }
    }
  }
}
