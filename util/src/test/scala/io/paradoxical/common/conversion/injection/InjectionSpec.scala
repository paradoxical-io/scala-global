//
// InjectionSpec.scala
package io.paradoxical.common.conversion.injection

import io.paradoxical.common.conversion.bijection.Bijection
import io.paradoxical.common.conversion.injection.Injection._
import org.scalatest.{FlatSpec, Matchers}
import scala.util.Success

class InjectionSpec extends FlatSpec with Matchers {
  private val stringToLongBijection = unsafeToBijection(stringToLong).inverse

  "Injection" should "be composable with andThen" in {
    val intToBigInt = intToLong andThen longToBigInt

    intToBigInt(1) should equal(BigInt(1))
    intToBigInt.invert(BigInt(1)) should equal(Success(1))
    intToBigInt.invert(BigInt(Long.MaxValue)).isFailure shouldBe true
  }

  it should "be composeable with other injections" in {
    val intToBigInt = longToBigInt compose intToLong

    intToBigInt(1) should equal(BigInt(1))
    intToBigInt.invert(BigInt(1)) should equal(Success(1))
    intToBigInt.invert(BigInt(Long.MaxValue)).isFailure shouldBe true
  }

  it should "be composable with bijections" in {
    val stringToBigInt = longToBigInt compose stringToLongBijection

    stringToBigInt("1") should equal(BigInt(1))
    stringToBigInt.invert(BigInt(1)) should equal(Success("1"))
  }

  it should "be composable with functions" in {
    val longGetter: (Int) => Long = (i: Int) => i.toLong

    val intToBigInt = longToBigInt compose longGetter

    intToBigInt(1) should equal(BigInt(1))
  }

  it should "make an injection from an Option" in {
    val intOptionInjection = optionInjection[Int]

    intOptionInjection(1) should equal(Some(1))
    intOptionInjection.invert(Some(1)) should equal(Success(1))
    intOptionInjection.invert(None).isFailure shouldBe true
  }

  it should "make an unsafe Bijection" in {
    val unsafeIntBijection: Bijection[Int, Long] = unsafeToBijection(intToLong)

    unsafeIntBijection(1) should equal(1L)
    unsafeIntBijection.invert(1L) should equal(1)
    an[Exception] should be thrownBy unsafeIntBijection.invert(Long.MaxValue)
  }
}
