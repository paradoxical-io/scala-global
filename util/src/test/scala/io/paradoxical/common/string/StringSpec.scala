package io.paradoxical.common.string

import org.scalatest._

class StringSpec extends FlatSpec with Matchers {
  "combinePathSeparators" should "combine path separators" in {
    Strings.combinePathSeparators("//a///b////c//") shouldEqual "/a/b/c/"
  }

  "trimToOption" should "return Some(string) if non empty/null" in {
    Strings.trimToOption("a") shouldEqual Some("a")
  }

  it should "return None if empty" in {
    Strings.trimToOption("") shouldEqual None
  }

  it should "return None if null" in {
    Strings.trimToOption(null) shouldEqual None
  }
}
