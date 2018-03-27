
package io.paradoxical.common.extensions.file

import java.io.{File, FileFilter}
import java.nio.file.{FileSystems, Path, Paths}
import scala.util.matching.Regex

/**
  * Improves ease of use when working with java.nio.file.Path objects. Use the / operator
  * to combine two or more paths, e.g.:
  * {{{
  *   val pathObj: Path = Paths.get("/home/")
  *   val newPath: Path = pathObj / "user" / "baz"
  *   println(newPath)
  * }}}
  * This results in '/home/user/baz'
  *
  * The ls function offers an easy way to query files by pattern, regex, or function.
  * It returns a Seq[File] that can be mapped to perform some other operation.
  * {{{
  *   // Get a Seq containing the lines of each txt file
  *   val txtFiles: = "/home/user" ls "*.txt" map { f => Source.fromFile(f).getLines  }
  * }}}
  */
class RichPath(p: Path) {
  val file: File = p.toFile
  val path: Path = p

  /**
    * Merge multiple Path objects
    */
  def /(other: String): Path = p.resolve(other.stripPrefix("/")).normalize

  def /(other: Path): Path = /(other.toString)

  /**
    * Convenience method to provide same API between File and Path
    *
    * @return the name of the file itself, equivalent to POSIX basename
    */
  def baseName: String = path.getFileName.toString

  /**
    * List files filtered by regex
    */
  def ls(pattern: Regex): Seq[File] = {
    val matcher = FileSystems.getDefault.getPathMatcher("regex:" + pattern)
    file listFiles new FileFilter {
      override def accept(pathname: File): Boolean = matcher.matches(pathname.toPath.getFileName)
    } toSeq
  }

  /**
    * Lists files filtered with glob syntax, the format of which is described by the Java API:
    * https://docs.oracle.com/javase/tutorial/essential/io/fileOps.html#glob
    */
  def ls(glob: String): Seq[File] = {
    val matcher = FileSystems.getDefault.getPathMatcher("glob:" + glob)
    file listFiles new FileFilter {
      override def accept(pathname: File): Boolean = matcher.matches(pathname.toPath.getFileName)
    } toSeq
  }

  /**
    * List files filtered by predicate
    */
  def ls(predicate: File => Boolean): Seq[File] = {
    file listFiles new FileFilter {
      override def accept(pathname: File) = predicate(pathname)
    } toSeq
  }

  /**
    * @return an Option[File] if file exists on disk, None otherwise
    */
  def fileOption: Option[File] = {
    if (p.toFile.exists()) Some(p.toFile) else None
  }

  override def toString: String = p.toString
}

trait RichPathExtensions {
  implicit def pathToRichPath(p: Path): RichPath = new RichPath(p)

  implicit def pathToString(p: Path): String = p.toString

  implicit def richPathToPath(p: RichPath): Path = p.path

  implicit def richPathToString(p: RichPath): String = p.toString

  implicit def stringToPath(p: String): Path = Paths.get(p)

  implicit def stringToRichPath(p: String): RichPath = new RichPath(Paths.get(p))
}

object RichPath extends RichPathExtensions
