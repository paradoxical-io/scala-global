package io.paradoxical.common.string

class RichStrings(value: String) {
  def trimToOption: Option[String] = Strings.trimToOption(value)
}

/**
 * Provides safe functions to attempt to parse a number from a string
 *
 * @param str the string to be parsed
 */
class StringToNumber(str: String) {

  private def toNumOption[T](f: () => T): Option[T] = {
    try {
      Some(f())
    } catch {
      case e: Exception => None
    }
  }

  def toLongOption: Option[Long] = {
    toNumOption(() => str.toLong)
  }

  def toIntOption: Option[Int] = {
    toNumOption(() => str.toInt)
  }

  def toDoubleOption: Option[Double] = {
    toNumOption(() => str.toDouble)
  }

  def toFloatOption: Option[Float] = {
    toNumOption(() => str.toFloat)
  }

  def toShortOption: Option[Short] = {
    toNumOption(() => str.toShort)
  }

  def toBooleanOption: Option[Boolean] = {
    val v = str.toLowerCase
    if (v == "1" || v == "0" || v == "true" || v == "false") {
      Some(v == "1" || v == "true")
    } else {
      None
    }
  }
}