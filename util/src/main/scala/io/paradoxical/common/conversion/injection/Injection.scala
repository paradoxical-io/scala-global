//
// Injection.scala
package io.paradoxical.common.conversion.injection

import io.paradoxical.common.conversion.bijection.{AbstractBijection, Bijection}
import scala.util.{Failure, Success, Try}

/**
 * An Injection represents a one-way conversion from type A to type B. Some Injections are reversible (and thus can be seen
 * as a Bijection. This reverse conversion is not guaranteed, so the "invert" function on Injections return a Try[A]
 *
 * @tparam A
 * @tparam B
 * @see [[https://github.com/twitter/bijection/blob/develop/bijection-core/src/main/scala/com/twitter/bijection/Injection.scala Injection.scala]]
 */
trait Injection[A, B] { self =>
  def apply(a: A): B
  def invert(b: B): Try[A]

  /**
   * Alias for andThen with Injection
   *
   * @param g
   * @tparam C
   * @return
   */
  def |[C](g: Injection[B, C]): Injection[A, C] = andThen(g)

  /**
   * Alias for andThen with Bijection
   *
   * @param g
   * @tparam C
   * @return
   */
  def |[C](g: Bijection[B, C]): Injection[A, C] = andThen(g)

  /**
   * Alias for andThen with a function
   *
   * @param g
   * @tparam C
   * @return
   */
  def |[C](g: B => C): A => C = andThen(g)

  /**
   * Composes an [[Injection]][A, B] with another Injection[B, C] to create an Injection[A, C] (former is applied first)
   *
   * @param g
   * @tparam C
   * @return
   */
  def andThen[C](g: Injection[B, C]): Injection[A, C] = new AbstractInjection[A, C] {
    override def apply(a: A): C = g(self(a))
    override def invert(c: C): Try[A] = g.invert(c).flatMap(self.invert)
  }

  /**
   * Composes an [[Injection]][A, B] with a Bijection[B, C] to create an Injection[A, C] (former is applied first)
   *
   * @param g
   * @tparam C
   * @return
   */
  def andThen[C](g: Bijection[B, C]): Injection[A, C] = new AbstractInjection[A, C] {
    override def apply(a: A): C = g(self(a))
    override def invert(c: C): Try[A] = self.invert(g.invert(c))
  }

  /**
   * Composes a Bijection with a function from B => C to create a function from B => C
   *
   * @param g
   * @tparam C
   * @return
   */
  def andThen[C](g: (B => C)): (A => C) = g compose this.toFunction

  /**
   * Alias for compose with Bijection
   *
   * @param bc The Bijection to compose with
   * @tparam C The left-hand type of the resulting Bijection
   * @return A new Injection[C, B]
   */
  def &[C](bc: Bijection[C, A]): Injection[C, B] = compose(bc)

  /**
   * Alias for compose with Injection
   *
   * @param ij The Injection to compose with
   * @tparam C The left-hand type of the resulting Injection
   * @return A new Injection[C, B]
   */
  def &[C](ij: Injection[C, A]): Injection[C, B] = compose(ij)

  /**
   * Alias for compose with a function
   *
   * @param bc
   * @tparam C
   * @return A new function C => B
   * @see [[Injection.compose[T](g:T=>A)*]]
   */
  def &[C](bc: C => A): C => B = compose(bc)

  /**
   * Alias for compose with a 0-ary function
   *
   * @param bc
   * @see [[Injection.compose(g:()=>A)*]]
   */
  def &(bc: () => A): () => B = compose(bc)

  /**
   *
   * @param g
   * @tparam T
   * @return
   */
  def compose[T](g: Injection[T, A]): Injection[T, B] = g andThen this

  /**
   *
   * @param bij
   * @tparam T
   * @return
   */
  def compose[T](bij: Bijection[T, A]): Injection[T, B] = new AbstractInjection[T, B] {
    override def apply(a: T): B = self(bij(a))
    override def invert(b: B): Try[T] = self.invert(b).map(bij.invert)
  }

  /**
   * Composes this Injection
   *
   * @param g
   * @tparam T
   * @return
   */
  def compose[T](g: T => A): (T => B) = g andThen this.toFunction

  /**
   * Compose this Injection with a 0-ary function
   *
   * @param g
   * @return
   */
  def compose(g: () => A): () => B = () => this.toFunction.apply(g())

  /**
   * Cretes a function A => B from this Injection
   * @return
   */
  def toFunction: (A => B) = new InjectionFunction[A, B](this)
}

abstract class AbstractInjection[A, B] extends Injection[A, B] {
  override def apply(a: A): B
  override def invert(b: B): Try[A]
}

private[conversion] class InjectionFunction[A, B](injection: Injection[A, B]) extends (A => B) {
  override def apply(v1: A): B = injection(v1)
}

trait LowPriInjections {
  implicit def fromBijection[A, B](implicit bij: Bijection[A, B]): Injection[A, B] = new Injection[A, B] {
    override def apply(a: A): B = bij(a)
    override def invert(b: B): Try[A] = Success(bij.invert(b))
  }
}

trait HighPriInjections extends LowPriInjections {
  implicit def optionInjection[A]: Injection[A, Option[A]] = new AbstractInjection[A, Option[A]] {
    override def apply(a: A): Option[A] = Some(a)
    override def invert(b: Option[A]): Try[A] = b.fold(Try[A](throw new NoSuchElementException))(x => Success[A](x))
  }

  implicit def identityInjection[A]: Injection[A, A] = new AbstractInjection[A, A] {
    override def apply(a: A): A = a
    override def invert(b: A): Try[A] = Success(b)
  }
}

object Injection extends HighPriInjections with StringInjections with NumericInjections {
  def toPartial[A, C, B, D](fn: A => C)(implicit inj1: Injection[A, B], inj2: Injection[C, D]): PartialFunction[B, D] = {
    new PartialFunction[B, D] {
      override def isDefinedAt(b: B): Boolean = inj1.invert(b).isSuccess
      override def apply(b: B): D = inj2.apply(fn(inj1.invert(b).get))
    }
  }

  /**
   * Use of this implies you want exceptions when the inverse is undefined
   */
  def unsafeToBijection[A, B](implicit inj: Injection[A, B]): Bijection[A, B] =
    new AbstractBijection[A, B] {
      def apply(a: A): B = inj(a)
      override def invert(b: B): A = inj.invert(b) match {
        case Success(a) => a
        case Failure(t) => throw t
      }
    }
}
