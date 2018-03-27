//
// Inversion.scala
package io.paradoxical.common.conversion.injection

import scala.util.control.NonFatal
import scala.util.{Failure, Success, Try}

object Inversion {
  import InversionFailure._

  def attempt[A, B](b: B)(inv: B => A): Try[A] = Try(inv(b)).recoverWith(recover(b))

  def attemptWhen[A, B](b: B)(test: B => Boolean)(inv: B => A): Try[A] = {
    if (test(b)) Success(inv(b)) else recover(b).apply(new UnsupportedOperationException)
  }
}

object InversionFailure {
  def recover[A, B](b: B): PartialFunction[Throwable, Try[A]] = {
    case NonFatal(t) => Failure(new UnsupportedOperationException(s"Failed to invert $b", t))
  }
}
