package io.paradoxical.common.io

import java.io.File
import org.scalatest.{FlatSpec, Matchers}

class TemporaryFileHelperTest extends FlatSpec with Matchers {

  "Temporary File Helper " should "create a temporary file with a specific suffix/prefix" in {
    val prefix = "test-prefix"
    val suffix = "test-suffix"
    val tmpFile = TemporaryFileHelper.createTemporaryFile(prefix, suffix)
    val path = tmpFile.getPath
    tmpFile.delete

    assert(path.contains(prefix))
    assert(path.endsWith(suffix))
  }

  it should "create a temporary file in a specific directory" in {
    val prefix = "test-prefix"
    val suffix = "test-suffix"
    val folderPath = "/tmp/"
    val folder = Some(new File(folderPath))
    val tmpFile = TemporaryFileHelper.createTemporaryFile(prefix, suffix, folder)
    val path = tmpFile.getPath
    tmpFile.delete

    assert(path.contains(prefix))
    assert(path.endsWith(suffix))
    assert(path.contains(folderPath))
  }

  it should "delete the file when using the loaner pattern" in {
    val (existedInside, fName) = TemporaryFileHelper.withTemporaryFile(f => {
      (f.exists, f.getAbsolutePath)
    })
    assert(existedInside)
    assert(!new File(fName).exists)
  }

}
