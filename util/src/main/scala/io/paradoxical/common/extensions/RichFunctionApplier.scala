package io.paradoxical.common.extensions

class AppliableFunctionObject[T](obj: T) {
  def applyIf(cond: Boolean)(f: T => T): T = {
    if (cond) f(obj) else obj
  }

  def applyOptionalValue[U](opt: Option[U])(f: (T, U) => T): T = {
    if (opt.isDefined) f(obj, opt.get) else obj
  }
}
