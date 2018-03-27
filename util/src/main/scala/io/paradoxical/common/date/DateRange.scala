package io.paradoxical.common.date

import java.sql.Timestamp
import org.joda.time.{LocalDate, DateTime, DateTimeZone}
import org.joda.time.format.DateTimeFormat
import scala.annotation.tailrec

/**
  * DateRange contains a start and end date that are used inclusively. It is useful for generating a range
  * of dates to operate over.
  */
object DateRange {
  private val DATE_PATTERN = "yyyy-MM-dd"
  private val SEPARATOR = ":"
  private val DEFAULT_TZ = DateTimeZone.forID("America/New_York")

  val dateFormat = DateTimeFormat.forPattern(DATE_PATTERN)

  def apply(start: LocalDate, end: LocalDate): DateRange = {
    new DateRange(start, end)
  }

  /**
    * Get the current day (defaults in EST timezone)
    */
  def today(tz: DateTimeZone = DEFAULT_TZ): LocalDate = LocalDate.now(tz)

  /**
   * Parse a date range string into DateRange objects
   *
   * @param s Date range string, of form "2013-09-13:2013-10-03"
   * @return Option[DateRange]
   */
  def parse(s: String, separator: String = SEPARATOR, datePattern: String = DATE_PATTERN): Option[DateRange] = {
    try {
      val fmt = DateTimeFormat.forPattern(datePattern)
      s.split(separator).toList match {
        case from :: to :: Nil => {
          Some(DateRange(fmt.parseLocalDate(from.toString), fmt.parseLocalDate(to.toString)))
        }
        case _ => None
      }
    } catch {
      case e: Exception => None
    }
  }
}

class DateRange(val start: LocalDate, val end: LocalDate) {
  import DateRange._

  /**
    * Move both the start and end date backwards by i days
    *
    * @param i Number of days to shift backward
    * @return DateRange
    */
  def shiftBackDays(i: Int): DateRange = {
    DateRange(start.minusDays(i), end.minusDays(i))
  }

  /**
    * Move both the start and end date forwards by i days
    *
    * @param i Number of days to shift forward
    * @return DateRange
    */
  def shiftForwardDays(i: Int): DateRange = {
    DateRange(start.plusDays(i), end.plusDays(i))
  }

  /**
    * Move start date backward by i days, keeping end date the same
    *
    * @param i Number of days to shift start backward
    * @return DateRange
    */
  def extendBackDays(i: Int): DateRange = {
    DateRange(start.minusDays(i), end)
  }

  /**
    * Move start date forward by i days, keeping end date the same
    *
    * @param i Number of days to shift start forward
    * @return DateRange
    */
  def extendForwardDays(i: Int): DateRange = {
    DateRange(start, end.plusDays(i))
  }

  /**
    * Shift the entire date range backwards by the current amount of days in the range.
    *
    * @return DateRange
    */
  def prevDateRange: DateRange = shiftBackDays(days.size)

  /**
    * Return start and end as a long epoch (ms since 1970) from the start of the day (defaults to EST timezone)
    */
  def asEpoch(tz: DateTimeZone = DEFAULT_TZ): (Long, Long) = {
    (start.toDateTimeAtStartOfDay(tz).getMillis,
      end.toDateTimeAtStartOfDay(tz).plusDays(1).getMillis)
  }

  /**
    * Return start and end in java.sqlTimestamp format from the start of the day (defaults to EST timezone)
    */
  def asTimestamp(tz: DateTimeZone = DEFAULT_TZ): (Timestamp, Timestamp) = {
    (new Timestamp(start.toDateTimeAtStartOfDay(tz).getMillis),
      new Timestamp(end.toDateTimeAtStartOfDay(tz).plusDays(1).getMillis))
  }

  /**
    * Turn the LocalDate into DateTime in the provided timezone
    *
    * @param tz Timezone to use (default EST)
    */
  def getDateTimeRange(tz: DateTimeZone = DEFAULT_TZ): DateTimeRange = {
    val start = new DateTime(this.start, tz)
    val end = new DateTime(this.end, tz)
    new DateTimeRange(start, end)
  }

  /**
    * Determine whether a given timestamp in epoch format is within a range
    *
    * @param tz Timezone to use (default EST)
    */
  def isTimeWithinRange(timestamp: Long, tz: DateTimeZone = DEFAULT_TZ): Boolean = {
    val epoch = asEpoch(tz)
    epoch._1 <= timestamp && epoch._2 > timestamp
  }

  /**
    * Gets a list of days to operate on.
    */
  def days: Iterable[LocalDate] = getDaysRecursive()

  /**
    * Get an OpenDateRange object, that is by definition "closed" because start and end exist in a DateRange
    */
  def getOpenDateRange: OpenDateRange = OpenDateRange(Some(start), Some(end))

  @tailrec
  private def getDaysRecursive(daysSoFar: List[LocalDate] = Nil, currentDate: LocalDate = start): List[LocalDate] = {
    if (currentDate.isAfter(end)) {
      daysSoFar
    } else {
      val newList = (currentDate :: daysSoFar.reverse).reverse
      getDaysRecursive(newList, currentDate.plusDays(1))
    }
  }
  //
  // Strategy for overriding equals()/hashCode() taken from 'Programming in Scala', 1st Edition:
  // http://www.artima.com/pins1ed/object-equality.html#28.4
  //

  override def equals(other: Any): Boolean = other match {
    case that: DateRange => (that canEqual this) &&
      this.start == that.start &&
      this.end == that.end
    case _ => false
  }

  def canEqual(other: Any): Boolean = other.isInstanceOf[DateRange]

  override def hashCode: Int = {
    41 * (
      41 + start.hashCode
    ) + end.hashCode
  }

  override def toString: String = {
    start.toString(dateFormat) + SEPARATOR + end.toString(dateFormat)
  }
}
