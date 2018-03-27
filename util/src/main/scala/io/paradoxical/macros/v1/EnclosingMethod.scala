package io.paradoxical.macros.v1

import macrocompat.bundle
import scala.annotation.tailrec
import scala.reflect.macros.whitebox

case class EnclosingMethod(
  methodName: String,
  className: String
)

object EnclosingMethods {
  implicit def enclosingMethodName: EnclosingMethod = macro EnclosingMethodsMacro.enclosingMethodNameMacro
}

@bundle
class EnclosingMethodsMacro(val c: whitebox.Context) {
  import c.universe._

  def enclosingMethodNameMacro: Tree = {
    val enclosingOwner = c.internal.enclosingOwner.asTerm

    @tailrec
    def getClassSym(sym: Symbol): String = {
      if(sym.isClass) {
        sym.name.decodedName.toString
      } else {
        getClassSym(sym.owner)
      }
    }

    @tailrec
    def getMethodSym(sym: Symbol): String = {
      if(sym.isMethod) {
        sym.name.decodedName.toString
      } else {
        getMethodSym(sym.owner)
      }
    }

    def toLiteral(s: String): c.Expr[String] = {
      c.Expr[String](Literal(Constant(s)))
    }

    reify {
      EnclosingMethod(
        methodName = toLiteral(getMethodSym(enclosingOwner)).splice,
        className = toLiteral(getClassSym(enclosingOwner)).splice
      )
    }.tree
  }
}
