package io.paradoxical.common.date

import org.joda.time.{DateTimeZone, LocalDate}

/**
  * OpenDateRange is similar to DateRange except that it is not required to have a start or an end. If a start or
  * end is not provided, then it defaults to today. The syntax is typically:
  *
  *   start only:     "2017-04-14:"
  *   end only:       ":2017-04-14"
  *   start and end:  "2017-04-14:2017-04-15"
  *   only today:     ":"
  */
object OpenDateRange {
  private val SEP = ":"

  def parse(str: String, sep: String = SEP): Option[OpenDateRange] = {

    try {
      val fmt = DateRange.dateFormat
      val parts = str.split(sep).toList

      val (start, end) = parts match {
        case "" :: e :: Nil if str.startsWith(sep)  => (None, Some(fmt.parseLocalDate(e)))
        case s  :: e :: Nil                         => (Some(fmt.parseLocalDate(s)), Some(fmt.parseLocalDate(e)))
        case s  :: Nil if str.endsWith(sep)         => (Some(fmt.parseLocalDate(s)), None)
        case Nil if str == sep                      => (None, None)
        case _                                      => throw new IllegalArgumentException("Invalid format: " + str)
      }

      Some(OpenDateRange(start, end))
    } catch {
      case e: Exception => None
    }
  }

  def asString(start: Option[LocalDate], end: Option[LocalDate]): String = {
    start.map(_.toString(DateRange.dateFormat)).getOrElse("") + SEP + end.map(_.toString(DateRange.dateFormat)).getOrElse("")
  }
}

case class OpenDateRange(start: Option[LocalDate], end: Option[LocalDate]) {
  import OpenDateRange._

  def hasStart: Boolean = start.isDefined
  def hasEnd: Boolean = end.isDefined
  def getDateRange(defaultStart: => LocalDate, defaultEnd: => LocalDate = DateRange.today()): DateRange = {
    DateRange(start.getOrElse(defaultStart), end.getOrElse(defaultEnd))
  }

  def withTimeZone(tz: DateTimeZone): OpenDateRangeTz = {
    OpenDateRangeTz(start, end, tz)
  }

  override def toString: String = asString(start, end)
}

case class OpenDateRangeTz(start: Option[LocalDate], end: Option[LocalDate], tz: DateTimeZone) {
  import OpenDateRange._

  override def toString: String = asString(start, end)
}
