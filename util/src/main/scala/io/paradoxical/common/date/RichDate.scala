package io.paradoxical.common.date

import org.joda.time._

class DateTimePlus(dt: DateTime) {
  def toSqlTimestamp: java.sql.Timestamp = {
    new java.sql.Timestamp(dt.getMillis)
  }

  /**
   * In some places in our codebase, we store a local time as an int/long representing the number of milliseconds from the
   * start of the day. For example, 2:00 AM would be 2 * 3600 * 1000 (3600 seconds in an hour and 1000 ms in a second).
   * this works great every day *except* the start and end of daylight savings time. On these days, there is an extra hour
   * inserted or removed. For example, on the day the clock moves back at 2 AM, a millisecond value for 10 AM (10 * 3600 * 1000)
   * will actually result in a DateTime with a time componenet of 9 AM if the following code is used:
   * dt.withTimeAtStartOfDay.plusMillis(10 * 3600 * 1000). This function gets us a DateTime with the correct time component.
   *
   * Also of note is that there currently appears to be a bug in joda time around DST. The getter/setter for millisOfDay are not aligned.
   * It is best demonstrated with this REPL output. Note that 11/3 is the day the clock moves back in 2013:
   * scala> (new DateTime(2013, 11, 3, 0, 0)).plusMillis(10 * 3600 * 1000).getMillisOfDay == (10 * 3600 * 1000)
   * res23: Boolean = false
   * scala> (new DateTime(2013, 11, 3, 0, 0)).plusMillis(10 * 3600 * 1000).getMillisOfDay == (9 * 3600 * 1000)
   * res24: Boolean = true
   *
   * @param ms
   * @return
   */
  def withTimeDstSafe(ms: Long): DateTime = {
    val lt = DateTimes.long2LocalTime(ms)
    withTimeDstSafe(lt)
  }

  def withTimeDstSafe(lt: LocalTime): DateTime = {
    dt.withTime(lt.getHourOfDay, lt.getMinuteOfHour, lt.getSecondOfMinute, lt.getMillisOfSecond)
  }
}

class LocalDatePlus(ld: LocalDate) {
  def toSqlDate: java.sql.Date = {
    new java.sql.Date(ld.toDateTimeAtStartOfDay(DateTimeZone.getDefault).getMillis)
  }
}