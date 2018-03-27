//
// StringBijection.scala
package io.paradoxical.common.conversion.bijection

trait StringBijections {
  implicit val symbolToString: Bijection[Symbol, String] = new AbstractBijection[Symbol, String] {
      def apply(s: Symbol): String = s.name
      override def invert(s: String): Symbol = Symbol(s)
    }
}
