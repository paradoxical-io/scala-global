//
// Bijection.scala
package io.paradoxical.common.conversion.bijection

import io.paradoxical.common.conversion.injection.Injection
import scala.annotation.implicitNotFound
import scala.reflect.ClassTag

abstract class AbstractBijection[A, B] extends Bijection[A, B] {
  override def apply(a: A): B
  override def invert(b: B): A
}

/**
 * Factory and implicits for [[Bijection]]
 */
object Bijection extends StringBijections with NumericBijections with BinaryBijections {
  implicit def toFunction[A, B](bijection: Bijection[A, B]): (A => B) = bijection.toFunction

  def apply[A, B](a: A)(implicit bij: ImplicitBijection[A, B]): B = bij.bijection(a)
  def invert[A, B](b: B)(implicit bij: ImplicitBijection[A, B]): A = bij.bijection.invert(b)

  /**
   * Builds a Bijection from two functions
   *
   * @param to Function for converting A => B
   * @param from Function for converting B => A
   * @tparam A
   * @tparam B
   * @return A new Bijection[A, B]
   */
  def build[A, B](to: A => B)(from: B => A): Bijection[A, B] = new AbstractBijection[A, B] {
    override def apply(a: A): B = to(a)
    override def invert(b: B): A = from(b)
  }

  /**
   * Builds a Bijection between a type A and a subtype B
   * The conversion happens automatically if the class B can be assigned from A, otherwise the provided function will be used
   *
   * @param afn Converts from A to B, if the class can't be assigned directly
   * @param ct
   * @tparam A
   * @tparam B
   * @return
   */
  def subclass[A, B <: A](afn: A => B)(implicit ct: ClassTag[B]): Bijection[A, B] =
    new SubclassBijection[A, B](ct.runtimeClass.asInstanceOf[Class[B]]) {
      def applyfn(a: A): B = afn(a)
    }
}

private[bijection] class BijectionFunction[A, B](bij: Bijection[A, B]) extends (A => B) {
  override def apply(v1: A): B = bij(v1)
}

/**
 * A Bijection represents a two-way conversion relationship between two types. Type A can always
 * be reliably converted to an instance of type B and vice versa. Bijections can be composed with
 * other bijections, injections (see [[io.paradoxical.common.conversion.injection.Injection]]), and functions
 *
 * {{{
 *   val
 * }}}
 * @tparam A Type that can be converted to B
 * @tparam B Type that can be converted to A
 * @see [[https://github.com/twitter/bijection/blob/develop/bijection-core/src/main/scala/com/twitter/bijection/Bijection.scala Bijection.scala]]
 */
@implicitNotFound(msg = "Cannot find Bijection type class between ${A} and ${B}")
trait Bijection[A, B] { self =>
  def apply(a: A): B
  def invert(b: B): A = inverse(b)

  /**
   * Creates the inverse of this Bijection
   * @return
   */
  def inverse: Bijection[B, A] =
    new AbstractBijection[B, A] {
      override def apply(a: B): A = self.invert(a)
      override def invert(b: A): B = self(b)
      override def inverse: Bijection[A, B] = self
    }

  /**
   * Alias for andThen with Bijection
   *
   * @param bc The Bijection to pipe through
   * @tparam C Right-side type for the resulting Bijection
   * @return A [[Bijection]][A, C]
   */
  def |[C](bc: Bijection[B, C]): Bijection[A, C] = andThen(bc)

  /**
   * Alias for andThen with Injection
   *
   * @param ij The Injection to pipe through
   * @tparam C Right-side type for the resulting Injection
   * @return An [[io.paradoxical.common.conversion.injection.Injection Injection]][A, C]
   */
  def |[C](ij: Injection[B, C]): Injection[A, C] = andThen(ij)

  /**
   * Alias for andThen with a function
   *
   * @param f The function to pipe through
   * @tparam C The resulting type of the function
   * @return A new function mapping A => C
   */
  def |[C](f: (B => C)): (A => C) = andThen(f)

  /**
   * Composes a [[Bijection]][A, B] with a Bijection[B, C] to create a Bijection[A, C] (former is applied first)
   *
   * {{{
   *   val stringToLong = new Bijection[String, Long] { ... }
   *   val longToBigInt = new Bijection[Long, BigInt] { ... }
   *   val stringToBigInt = stringToLong andThen longToBigInt
   * }}}
   *
   * @param bc The Bijection to pipe through
   * @tparam C Right-side type for the resulting Bijection
   * @return A [[Bijection]][A, C]
   */
  def andThen[C](bc: Bijection[B, C]): Bijection[A, C] = {
    new Bijection[A, C] {
      override def apply(a: A): C = bc(self(a))
      override def invert(b: C): A = self.invert(bc.invert(b))
    }
  }

  /**
   * Composes a Bijection with a [[io.paradoxical.common.conversion.injection.Injection Injection]][B, C] to create an Injection[A, C]
   *
   * {{{
   *
   * }}}
   *
   * @param ij The Injection to pipe through
   * @tparam C Right-side type for the resulting Injection
   * @return An [[io.paradoxical.common.conversion.injection.Injection Injection]][A, C]
   */
  def andThen[C](ij: Injection[B, C]): Injection[A, C] = ij compose this

  /**
   * Composes a Bijection with a function from B => C to create a function from B => C
   *
   * {{{
   * val stringToBytes = new Bijection[String, Array[Byte]]
   * val bytesToBuffer = new Bijection[Array[Byte], ByteBuffer]
   * val stringToBuffer = stringToBytes andThen bytesToBuffer
   * }}}
   *
   * @param g The function to pipe through
   * @tparam C The resulting type of the function
   * @return A new function mapping A => C
   */
  def andThen[C](g: (B => C)): (A => C) = g compose this.toFunction

  /**
   * Alias for compose with Bijection
   *
   * @param bc The Bijection to compose with
   * @tparam C The left-hand type of the resulting Bijection
   * @return A new Bijection[C, B]
   */
  def &[C](bc: Bijection[C, A]): Bijection[C, B] = compose(bc)

  /**
   * Alias for compose with Injection
   *
   * @param ij The Injection to compose with
   * @tparam C The left-hand type of the resulting Injection
   */
  def &[C](ij: Injection[C, A]): Injection[C, B] = compose(ij)

  /**
   * Alias for compose with a function
   *
   * @param bc
   * @tparam C
   * @see [[Bijection.compose[C](g:C=>A)*]]
   */
  def &[C](bc: C => A): C => B = compose(bc)

  /**
   * Composes this Bijection[A, B] with a Bijection[C, A] to create an Bijection[C, B]
   *
   * {{{
   * val stringToBytes = new Bijection[String, Array[Byte]]
   * val symbolToString = new Bijection[Symbol, String]
   * val symbolToBytes = stringToBytes compose symbolToString
   * }}}
   *
   * @param bc
   * @tparam C
   * @return
   */
  def compose[C](bc: Bijection[C, A]): Bijection[C, B] = bc andThen this

  /**
   * Composes this Bijection[A, B] with an Injection[C, A] to create an Injection[C, B]
   *
   * {{{
   *
   * }}}
   *
   * @param g
   * @tparam C
   * @return
   */
  def compose[C](g: Injection[C, A]): Injection[C, B] = g andThen this

  /**
   * Composes this Bijection[A, B] with a function C => A to create a function C => B
   *
   * {{{
   * val symbolToString = Bijection[Symbol, String]
   * val symbolFromInt: (Int => Symbol) = ...
   * val intToString = symbolToString compose symbolFromInt
   * }}}
   *
   * @param g
   * @tparam C
   * @return
   */
  def compose[C](g: (C => A)): (C => B) = g andThen this.toFunction

  /**
   *
   * {{{
   *
   * }}}
   *
   * @return
   */
  def toFunction: (A => B) = new BijectionFunction(self)
}

class IdentityBijection[A] extends Bijection[A, A] {
  override def apply(a: A): A = a
  override val inverse = this

  override def andThen[C](bc: Bijection[A, C]): Bijection[A, C] = bc
  override def compose[C](bc: Bijection[C, A]): Bijection[C, A] = bc
}

abstract class SubclassBijection[A, B <: A](clb: Class[B]) extends Bijection[A, B] {
  protected def applyfn(a: A): B
  def apply(a: A): B = {
    if (clb.isAssignableFrom(a.getClass)) {
      a.asInstanceOf[B]
    } else {
      applyfn(a)
    }
  }
  override def invert(b: B): A = b
}
