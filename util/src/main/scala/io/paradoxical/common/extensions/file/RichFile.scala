
package io.paradoxical.common.extensions.file

import java.io.File
import java.nio.file.attribute.PosixFilePermission
import java.nio.file.Files
import scala.collection.JavaConverters._

trait RichFileExtensions {
  implicit def fileToRichFile(f: File): RichFile = new RichFile(f)

  implicit def richFileToFile(f: RichFile): File = f.file
}

class RichFile(f: File) {
  val file: File = f
  private val path = f.toPath

  def addPermissions(permissions: PosixFilePermission*): Unit = {
    val perms = Files.getPosixFilePermissions(path)
    perms.addAll(permissions.asJava)
    Files.setPosixFilePermissions(path, perms)
  }

  def permissions: Set[PosixFilePermission] = {
    Files.getPosixFilePermissions(f.toPath).asScala.toSet
  }

  /**
    * Convenience method to provide same API between File and Path
    *
    * @return the name of the file itself, equivalent to POSIX basename
    */
  def baseName: String = path.getFileName.toString

  /**
    * @return the File object if it exists on disk, None otherwise
    */
  def existsOption: Option[File] = {
    if (f.exists()) Some(f) else None
  }
}

object RichFile extends RichFileExtensions
