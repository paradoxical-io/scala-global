//
// ImplicitBijection.scala
package io.paradoxical.common.conversion.bijection

// Use when defining a Bijection as an implicit parameter so that
// we get checks for both Bijection[A, B] and Bijection[B, A]
// which the type system thinks are separate
/**
 * Use when defining a Bijection as an implicit parameter so that
 * we get checks for both Bijection[A, B] and Bijection[B, A]
 * which the type system thinks are separate
 * @tparam A
 * @tparam B
 */
sealed trait ImplicitBijection[A, B] {
  def bijection: Bijection[A, B]
  def apply(a: A): B = bijection.apply(a)
  def invert(b: B): A = bijection.invert(b)
}

/**
 * Encodes the Bijection[A, B] with A, B in order
 * @param bijection
 * @tparam A
 * @tparam B
 */
case class BijectionAB[A, B](override val bijection: Bijection[A, B]) extends ImplicitBijection[A, B]

/**
 * Encodes the Bijection[A, B] with A, B in reverse order
 * @param inv
 * @tparam A
 * @tparam B
 */
case class BijectionBA[A, B](inv: Bijection[B, A]) extends ImplicitBijection[A, B] {
  val bijection = inv.inverse
}

trait LowPriorityImplicitBijection {
  implicit def reverse[A, B](implicit bij: Bijection[B, A]): ImplicitBijection[A, B] = BijectionBA(bij)
}

trait HighPriorityImplicitBijection extends LowPriorityImplicitBijection {
  implicit def forward[A, B](implicit bij: Bijection[A, B]): ImplicitBijection[A, B] = BijectionAB(bij)
}

object ImplicitBijection extends HighPriorityImplicitBijection
