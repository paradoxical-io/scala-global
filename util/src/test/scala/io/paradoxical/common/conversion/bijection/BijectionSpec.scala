//
// BijectionSpec.scala
package io.paradoxical.common.conversion.bijection

import io.paradoxical.common.conversion.bijection.Bijection._
import io.paradoxical.common.conversion.injection.Injection
import io.paradoxical.common.conversion.injection.Injection._
import org.scalatest.{FlatSpec, Matchers}

class BijectionSpec extends FlatSpec with Matchers {
  val stringToLongBijection = unsafeToBijection(stringToLong).inverse

  "Bijection" should "be composable with other bijections" in {
    def testStringToJavaLong(bijection: Bijection[String, java.lang.Long]): Unit = {
      val jLong = bijection("123")
      jLong shouldBe a[java.lang.Long]
      jLong should equal(123L)
    }

    testStringToJavaLong(stringToLongBijection andThen longToBoxed)
    testStringToJavaLong(longToBoxed compose stringToLongBijection)
  }

  it should "be composable with injections" in {
    def testStringToBigInt(injection: Injection[String, BigInt]): Unit = {
      val bigInt = injection("123")
      bigInt should equal(BigInt(123))
    }

    val injection1 = stringToLongBijection andThen longToBigInt
    testStringToBigInt(injection1)

    val injection2 = longToBigInt compose stringToLongBijection
    testStringToBigInt(injection2)
  }

  it should "be composable with functions" in {
    def testStringToInt(func: String => Int): Unit = {
      val int = func("123")
      int should equal(123)
    }

    val func1 = stringToLongBijection andThen (x => x.toInt)
    testStringToInt(func1)

    val func2 = ((x: Long) => x.toInt) compose stringToLongBijection
    testStringToInt(func2)
  }
}
