//
// NumericBijections.scala
package io.paradoxical.common.conversion.bijection

import java.lang.{Double => JDouble, Float => JFloat, Integer => JInt, Long => JLong}
import java.math.BigInteger

trait NumericBijections {
  implicit val intToBoxed: Bijection[Int, JInt] = new AbstractBijection[Int, JInt] {
      def apply(i: Int): JInt = JInt.valueOf(i)
      override def invert(i: JInt): Int = i.intValue
    }

  implicit val longToBoxed: Bijection[Long, JLong] = new AbstractBijection[Long, JLong] {
    def apply(l: Long): JLong = JLong.valueOf(l)
    override def invert(l: JLong): Long = l.longValue
  }

  implicit val bigInt2BigInteger: Bijection[BigInt, BigInteger] = new AbstractBijection[BigInt, BigInteger] {
    def apply(bi: BigInt): BigInteger = bi.bigInteger
    override def invert(jbi: BigInteger): BigInt = new BigInt(jbi)
  }

  implicit val float2Boxed: Bijection[Float, JFloat] = new AbstractBijection[Float, JFloat] {
    def apply(f: Float): JFloat = JFloat.valueOf(f)
    override def invert(f: JFloat): Float = f.floatValue
  }

  implicit val double2Boxed: Bijection[Double, JDouble] = new AbstractBijection[Double, JDouble] {
    def apply(d: Double): JDouble = JDouble.valueOf(d)
    override def invert(d: JDouble): Double = d.doubleValue
  }
}
