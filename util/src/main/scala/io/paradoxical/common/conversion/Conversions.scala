//
// Conversions.scala
package io.paradoxical.common.conversion

import io.paradoxical.common.conversion.bijection.ImplicitBijection

// Type class
case class Convert[A](a: A) extends AnyVal {
  def as[B](implicit conv: Conversion[A, B]): B = conv(a)
}

// Looks like a function, but we don't want a subclass relationship
trait Conversion[A, B] extends Serializable {
  def apply(a: A): B
}

trait LowPriorityConversion {
  implicit def fromBijection[A, B](implicit fn: ImplicitBijection[A, B]) = new Conversion[A, B] {
    def apply(a: A) = fn.bijection.apply(a)
  }
}

object Conversion extends LowPriorityConversion {
  implicit def asMethod[A](a: A): Convert[A] = Convert(a)
  // Both Injection and Bijection subclass (A) => B
  implicit def fromFunction[A, B](implicit fn: A => B) = new Conversion[A, B] {
    def apply(a: A) = fn(a)
  }
}
