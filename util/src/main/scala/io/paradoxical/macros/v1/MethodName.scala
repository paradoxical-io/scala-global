package io.paradoxical.macros.v1

import macrocompat.bundle
import scala.annotation.tailrec
import scala.language.experimental.macros
import scala.reflect.macros.whitebox

case class MethodName(name: String)

object MethodNames {
  implicit def methodName[A](extractor: (A) => Any): MethodName = macro MethodNamesMacro.methodNamesMacro[A]
}

@bundle
class MethodNamesMacro(val c: whitebox.Context) {
  import c.universe._

  def methodNamesMacro[A: c.WeakTypeTag](extractor: Tree): Tree = {
    @tailrec
    def resolveFunctionName(f: Function): String = {
      f.body match {
        // the function name
        case t: Select => t.name.decodedName.toString

        case t: Function => resolveFunctionName(t)

        // an application of a function and extracting the name
        case Apply(Select(_, selectName), _) => selectName.decodedName.toString

        // curried lambda
        case Block(_, f @ Function(_, _)) => resolveFunctionName(f)

        case _ => {
          throw new RuntimeException("Unable to resolve function name for expression: " + f.body)
        }
      }
    }

    val name = resolveFunctionName(extractor.asInstanceOf[Function])

    val literal = c.Expr[String](Literal(Constant(name)))

    reify {
      MethodName(literal.splice)
    }.tree
  }
}
