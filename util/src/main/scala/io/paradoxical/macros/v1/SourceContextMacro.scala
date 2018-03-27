package io.paradoxical.macros.v1

import macrocompat.bundle
import scala.language.experimental.macros
import scala.reflect.macros.whitebox

object SourceContext {
  implicit def sourceContext: SourceContext = macro SourceContextMacro.sourceContextImpl
}

@bundle
class SourceContextMacro(val c: whitebox.Context) {
  import c.universe._

  def sourceContextImpl: Tree = {
    val pos = c.enclosingPosition
    val fileName = pos.source.file.name
    val lineNo = pos.line

    reify {
      SourceContext(c.Expr[String](q"$fileName").splice, c.Expr[Int](q"$lineNo").splice)
    }.tree
  }
}

case class SourceContext(
  fileName: String,
  line: Int
)
