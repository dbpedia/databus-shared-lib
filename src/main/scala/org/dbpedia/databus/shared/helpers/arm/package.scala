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
