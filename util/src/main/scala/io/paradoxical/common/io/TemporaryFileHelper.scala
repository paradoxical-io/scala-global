package io.paradoxical.common.io

import java.io.File

object TemporaryFileHelper {
  private val DEFAULT_PREFIX = "tmp."
  private val DEFAULT_SUFFIX = ".tmp"

  /**
   * Creates a new temporary file on the disk.
   * @param prefix     The desired prefix if not default.
   * @param suffix     The desired suffix if not default
   * @param directory  Where the temporary file should be located. If not specified, the system's temporary directory is used.
   * @return
   */
  def createTemporaryFile(prefix: String = DEFAULT_PREFIX,
                          suffix: String = DEFAULT_SUFFIX,
                          directory: Option[File] = None): File = {
    directory.map(File.createTempFile(prefix, suffix, _)).getOrElse(File.createTempFile(prefix, suffix))
  }

  /**
   * Creates a new temporary file, executes the provided closure, and erases the temporary file when complete.
   * @param f   The closure to execute.
   * @tparam T  The return type of f.
   */
  def withTemporaryFile[T](f:(File) => T): T = {
    val tmpFile = createTemporaryFile()
    try {
      f(tmpFile)
    } finally {
      if (!tmpFile.delete) {
        throw new RuntimeException(s"Could not delete temporary file ${tmpFile.getAbsolutePath}")
      }
    }
  }
}