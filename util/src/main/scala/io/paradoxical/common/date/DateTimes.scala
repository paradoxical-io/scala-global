package io.paradoxical.common.date

import java.sql._
import org.joda.time._
import org.joda.time.format.DateTimeFormat

object DateTimes {
  val EST = DateTimeZone.forID("America/New_York")

  def durationSince(date: java.util.Date): Duration = {
    val before = new DateTime(date)
    new Duration(before, DateTime.now())
  }

  def moreRecent(a: Option[DateTime], b: Option[DateTime]): Option[DateTime] = {
    if (a.isDefined && b.isDefined) {
      if (a.get.getMillis >= b.get.getMillis) {
        a
      } else {
        b
      }
    } else if (a.isDefined) {
      a
    } else {
      b
    }
  }

  // between joda locals and java.sql/java.util
  implicit def localDateTime2Timestamp(l: LocalDateTime): Timestamp = new Timestamp(l.toDate.getTime)

  def timestamp2LocalDateTime(t: Timestamp): LocalDateTime = new LocalDateTime(t.getTime)

  def timestamp2UTCDateTime(t: Timestamp): DateTime = timestamp2LocalDateTime(t).toDateTime(DateTimeZone.UTC)

  implicit def localDate2Date(l: LocalDate): Date = new Date(l.toDate.getTime)

  implicit def date2LocalDate(d: java.util.Date): LocalDate = new LocalDate(d.getTime)

  implicit def long2LocalTime(t: Long): LocalTime = (new LocalTime(0, 0)).plusMillis(t.toInt)

  implicit def localTime2Long(t: LocalTime): Long = t.getMillisOfDay.toLong

  // joda datetime and java.sql
  implicit def timestamp2DateTime(ts: java.sql.Timestamp): DateTime = new DateTime(ts.getTime)

  implicit def dateTime2Timestamp(l: DateTime): Timestamp = new Timestamp(l.getMillis)

  // joda local and long
  def localDate2Long(d: LocalDate): Long = d.toDateTimeAtStartOfDay(EST).getMillis

  // Objects for specific conversions to avoid ambiguous implicits
  // Naming convention is alphabeticalFirst2AlphabeticalSecond
  object DateTime2Long {
    implicit def long2DateTime(l: Long): DateTime = new DateTime(l)

    implicit def dateTime2Long(d: DateTime): Long = d.getMillis

    implicit def longOption2DateTimeOption(l: Option[Long]): Option[DateTime] = l.map(long2DateTime)

    implicit def dateTimeOption2LongOption(dt: Option[DateTime]): Option[Long] = dt.map(dateTime2Long)
  }

  // Joda orderings

  implicit def jodaDateTimeOrdering: Ordering[DateTime] = Ordering.fromLessThan(_ isBefore _)

  implicit def jodaLocalDateOrdering: Ordering[LocalDate] = Ordering.fromLessThan(_ isBefore _)

  implicit def jodaLocalTimeOrdering: Ordering[LocalTime] = Ordering.fromLessThan(_ isBefore _)

  /**
   * Parses a DateTime object from the provided string and pattern.
   * The date is in the specified timezone if provided.
   *
   * @param v       The date in string format to parse
   * @param pattern The date/time pattern used to parse the date. See http://joda-time.sourceforge.net/apidocs/org/joda/time/format/DateTimeFormat.html
   * @param tz      The timezone of the date/time string.
   * @return       A DateTime object, or None if the parsing failes
   */
  def parseDateTime(v: String, pattern: String, tz: DateTimeZone): Option[DateTime] = {
    try {
      val formatter = DateTimeFormat.forPattern(pattern).withZone(tz)
      Some(formatter.parseDateTime(v))
    } catch {
      case i: IllegalArgumentException => None // the provided pattern/value could not be parsed
    }
  }

  private val MINUTE_SECONDS = 60
  private val HOUR_SECONDS = MINUTE_SECONDS * 60
  private val DAY_SECONDS = HOUR_SECONDS * 24
  private val WEEK_SECONDS = DAY_SECONDS * 7
  private val YEAR_SECONDS = WEEK_SECONDS * 52

  def prettyRelativeTime(timestamp: Long): String = {
    val deltaSeconds = (System.currentTimeMillis - timestamp) / 1000
    val suffix = if (deltaSeconds > 0) " ago" else " from now"
    val seconds = Math.abs(deltaSeconds)

    val relativeTime =
      if (seconds < MINUTE_SECONDS) {"Less than a minute"}
      else if (seconds < MINUTE_SECONDS * 2) {"1 minute"}
      else if (seconds < HOUR_SECONDS) {"%d minutes".format(seconds / MINUTE_SECONDS)}
      else if (seconds < HOUR_SECONDS * 2) {"1 hour"}
      else if (seconds < DAY_SECONDS) {"%d hours".format(seconds / HOUR_SECONDS)}
      else if (seconds < DAY_SECONDS * 2) {"1 day"}
      else if (seconds < WEEK_SECONDS) {"%d days".format(seconds / DAY_SECONDS)}
      else if (seconds < WEEK_SECONDS * 2) {"1 week"}
      else if (seconds < YEAR_SECONDS) {"%d weeks".format(seconds / WEEK_SECONDS)}
      else {"more than a year"}

    relativeTime + suffix
  }
}
