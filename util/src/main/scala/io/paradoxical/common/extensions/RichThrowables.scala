package io.paradoxical.common.extensions

import java.io.{PrintWriter, StringWriter}


class OptionWithThrow[T](o: Option[T]) {
  def getOrThrow[E <: Exception](e: => E): T = {
    o.getOrElse(throw e)
  }
}

object ThrowablePlus {
  // the max length of a text column is 64k, but 16k seems like more than enough
  // http://dev.mysql.com/doc/refman/5.0/en/storage-requirements.html
  private val MAX_STACK_LENGTH = 1024 * 16
}

class ThrowablePlus(t: Throwable) {

  import ThrowablePlus._

  def toStringWithStack(): String = {
    val sw = new StringWriter
    val pw = new PrintWriter(sw)
    t.printStackTrace(pw)
    pw.flush()
    sw.toString.substring(0, MAX_STACK_LENGTH)
  }

  def getMessageOption(): Option[String] = {
    Option(t.getMessage)
  }
}