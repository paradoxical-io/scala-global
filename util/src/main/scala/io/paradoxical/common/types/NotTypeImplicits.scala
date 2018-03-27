package io.paradoxical.common.types

/**
  * Encoding for "A is not a subtype of B"
  * @note original credit: https://gist.github.com/milessabin/c9f8befa932d98dcc7a4
  */
trait NotTypeOf[A, B]

trait NotTypeImplicits {
  // Uses ambiguity to rule out the cases we're trying to exclude
  implicit def allowedType[A, B] : A NotTypeOf B = null
  implicit def typeSuperTypeOfInvalid[A, B >: A] : A NotTypeOf B = null
  implicit def typeSuperTypeOfInvalidAmbiguous[A, B >: A] : A NotTypeOf B = null
}

object NotTypeImplicits extends NotTypeImplicits