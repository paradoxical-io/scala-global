//
// NumericInjections.scala
package io.paradoxical.common.conversion.injection

import io.paradoxical.common.conversion.injection.Inversion._
import scala.util.{Success, Try}

trait NumericInjections {
  implicit val intToLong: Injection[Int, Long] = new AbstractInjection[Int, Long] {
    def apply(i: Int): Long = i.toLong
    def invert(l: Long): Try[Int] = attemptWhen(l)(_.isValidInt)(_.toInt)
  }

  implicit val longToBigInt: Injection[Long, BigInt] = new AbstractInjection[Long, BigInt] {
    def apply(l: Long): BigInt = BigInt(l)
    def invert(bi: BigInt): Try[Long] = attemptWhen(bi)(bi => bi <= Long.MaxValue && Long.MinValue <= bi)(_.toLong)
  }

  implicit val floatToDouble: Injection[Float, Double] = new AbstractInjection[Float, Double] {
    def apply(i: Float): Double = i.toDouble
    def invert(l: Double): Try[Float] = attemptWhen(l)(l => l <= Float.MaxValue && l >= Float.MinValue)(_.toFloat)
  }

  implicit val intToDouble: Injection[Int, Double] = new AbstractInjection[Int, Double] {
    def apply(i: Int): Double = i.toDouble
    def invert(l: Double): Try[Int] = Success(l.toInt)
  }
}
