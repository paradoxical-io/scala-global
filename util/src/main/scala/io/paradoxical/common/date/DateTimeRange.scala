package io.paradoxical.common.date

import org.joda.time.{LocalTime, Duration, DateTime}

case class TimeRange(start: LocalTime, end: LocalTime)

/**
  * DateTimeRange: Similar to DateRange, except uses DateTime instead. The intervals function
  * is useful for generating a list of all the hours between two dates, or every 6 hours in a week, etc.
  */
case class DateTimeRange(start: DateTime, end: DateTime) {
  def intervals(intervalInMs: Long): Iterable[DateTimeRange] = IterableDateTimeRange(this, intervalInMs)
}


case class IterableDateTimeRange(range: DateTimeRange, intervalInMs: Long) extends Iterable[DateTimeRange] {
  if (intervalInMs < 1) {
    throw new IllegalArgumentException("intervalInMs must be at least 1")
  }

  // adding duration - 1 results in *non-overlapping* intervals of intervalInMs length. For example, if the interval is 10 and the starting point is zero,
  // we end up with intervals of 0-9, 10-19, 20-29...
  private val duration = new Duration(intervalInMs - 1)
  private var nextStart = range.start
  private def nextEnd = if (nextStart.plus(duration).isBefore(range.end)) nextStart.plus(duration) else range.end

  def iterator: Iterator[DateTimeRange] = new Iterator[DateTimeRange] {
    def hasNext: Boolean = !nextStart.isAfter(range.end)
    def next(): DateTimeRange = {
      if (!hasNext) {
        throw new RuntimeException("No more! hasNext is false")
      }

      val rangeToReturn = DateTimeRange(nextStart, nextEnd)
      nextStart = nextEnd.plusMillis(1)
      rangeToReturn
    }
  }
}
