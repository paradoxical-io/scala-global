package io.paradoxical.common.extensions

class RichNullable[A](val nullable: A) extends AnyVal {
  def ??(default: A): A = {
    Option(nullable).getOrElse(default)
  }
}
