package io.paradoxical.common.resources

import org.scalatest.{FlatSpec, Matchers}

class ResourcesTests extends FlatSpec with Matchers {
  "Resources" should "load with prefixed slash" in {
    Resources.load("/data/sample.txt").mkString shouldEqual "hello"
  }

  it should "load without prefixed slash" in {
    Resources.load("data/sample.txt").mkString shouldEqual "hello"
  }

  it should "load local files" in {
    assert(Resources.resourceFiles("data").map(_.getPath).mkString.contains("sample.txt"))
  }
}
