//
// StringBijectionSpec.scala
package io.paradoxical.common.conversion.bijection

import io.paradoxical.common.conversion.bijection.Bijection._
import org.scalatest.{FlatSpec, Matchers}

class StringBijectionSpec extends FlatSpec with Matchers {
  "StringBijection" should "convert to and from Symbols" in {
    val sym = Symbol("symbol")
    symbolToString(sym) should equal("symbol")
    symbolToString.invert(sym.name) should equal(sym)
  }
}
