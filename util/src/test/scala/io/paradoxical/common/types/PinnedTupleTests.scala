package io.paradoxical.common.types

import org.scalatest.{FlatSpec, Matchers}

object TestPinnedTuple extends PinnedStringTuple {
  override val key = "foo"
}

object TestPinnedTypedTuple extends PinnedTuple {
  override val key = "long"

  override type Value = Long

  override def parse(r: String): Long = r.toLong
}

class PinnedTupleTests extends FlatSpec with Matchers {
  "Pinned tuples" should "allow for key and value reading" in {
    TestPinnedTuple("data") shouldEqual TestPinnedTuple("data")

    TestPinnedTuple("data").toTuple match {
      case (TestPinnedTuple.key, "data") =>
      case _ => fail()
    }
  }
}
