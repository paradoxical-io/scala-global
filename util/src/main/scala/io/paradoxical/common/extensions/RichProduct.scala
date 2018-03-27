package io.paradoxical.common.extensions

import Extensions.productToRichProduct

class RichOptionProduct[T <: Product](p: Option[T]) {
  def orNoneIfAllFieldsAreNone: Option[T] = {
    p.flatMap {
      case c if c.allFieldsAreNone => None
      case _ => p
    }
  }
}

class RichProduct[T <: Product](p: T) {
  def allFieldsAreNone: Boolean = {
    !p.productIterator.exists(field => !field.isInstanceOf[Option[_]] || field.asInstanceOf[Option[_]].isDefined)
  }
}
