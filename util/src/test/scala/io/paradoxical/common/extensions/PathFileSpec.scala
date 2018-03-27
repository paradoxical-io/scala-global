package io.paradoxical.common.extensions

import io.paradoxical.common.extensions.file.RichFile._
import io.paradoxical.common.extensions.file.RichPath._
import java.io.FileWriter
import java.nio.file.attribute.PosixFilePermission
import java.nio.file.{Files, Path, Paths}
import org.scalatest.{BeforeAndAfter, FlatSpec, Matchers}

class PathFileSpec extends FlatSpec with Matchers with BeforeAndAfter {
  var tmpDir: Path = _

  before {
    tmpDir = Files.createTempDirectory("utils-test")
    Files.createDirectories(tmpDir / "abc")
    Files.createDirectories(tmpDir / "def")
  }

  after {
    tmpDir ls "*" foreach(_.delete())
  }

  it should "list files filtered with wildcard" in {
    tmpDir ls "*" map {_.baseName} should contain allOf ("abc", "def")
    tmpDir ls "d*" map {_.baseName} should contain("def")
    tmpDir ls "*c" map {_.baseName} should contain("abc")
  }

  it should "list files filtered with predicate" in {
    tmpDir ls (_.baseName == "abc") map {_.baseName} should contain("abc")
  }

  it should "list files filtered with regex" in {
    tmpDir ls "ab.*".r map { _.baseName } should contain("abc")
  }

  it should "resolve path concatenation correctly" in {
    ("/foo/bar/" / "../bin/./" / "./baz/../boo").toString shouldEqual "/foo/bin/boo"
  }

  it should "add permissions correctly" in {
    val file = tmpDir / "abc" toFile()
    file.permissions should not contain PosixFilePermission.OTHERS_WRITE
    file.addPermissions(PosixFilePermission.OTHERS_WRITE)
    file.permissions should contain(PosixFilePermission.OTHERS_WRITE)
  }

  "existsOption" should "be Some when file exists" in {
    val file = Files.createTempFile("a", "b")
    new FileWriter(file, false).write("test")
    file.toFile.existsOption should be (Some(file.toFile))
  }

  "existsOption" should "be None when file does not exist" in {
    Paths.get("doesntexit").toFile.existsOption should be (None)
  }

  "fileOption" should "be Some when file exists" in {
    val file = Files.createTempFile("a", "b")
    new FileWriter(file, false).write("test")
    file.fileOption should be (Some(file.toFile))
  }

  "fileOption" should "be None when file does not exist" in {
    Paths.get("doesntexit").fileOption should be (None)
  }
}
