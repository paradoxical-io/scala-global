package io.paradoxical.common.date

import org.joda.time.{DateTime, LocalDate}
import org.scalatest.{FlatSpec, Matchers}

class DateRangeSpec extends FlatSpec with Matchers {

  val START = new LocalDate(2014, 1, 1)
  val MID = START.plusDays(1)
  val END = MID.plusDays(1)
  val DELTA = 2
  val dr = DateRange(START, END)

  "DateRange" should "List the days in ascending order" in {
    dr.days.toList shouldEqual(List(START, MID, END))
  }

  it should "Shift the range back" in {
    dr.shiftBackDays(DELTA) shouldEqual(DateRange(START.minusDays(DELTA), END.minusDays(DELTA)))
  }

  it should "Shift the range forward" in {
    dr.shiftForwardDays(DELTA) shouldEqual(DateRange(START.plusDays(DELTA), END.plusDays(DELTA)))
  }

  it should "Extend the range back" in {
    dr.extendBackDays(DELTA) shouldEqual(DateRange(START.minusDays(DELTA), END))
  }

  it should "Extend the range forward" in {
    dr.extendForwardDays(DELTA) shouldEqual(DateRange(START, END.plusDays(DELTA)))
  }

  it should "Get the previous date range" in {
    dr.prevDateRange shouldEqual(dr.shiftBackDays(dr.days.size))
  }

  "A DateTimeRange" should "Produce the correct intervals with an interval of 1 ms" in {
    val dtStart = new DateTime(1)
    val dtEnd = dtStart.plusMillis(1)
    val dr = DateTimeRange(dtStart, dtEnd)
    val intervals = dr.intervals(1).toList

    intervals.size shouldEqual(2)
    intervals(0) shouldEqual(DateTimeRange(dtStart, dtStart))
    intervals(1) shouldEqual(DateTimeRange(dtEnd, dtEnd))
  }

  it should "Produce the correct intervals with an interval greater than 1 ms" in {
    val dtStart = new DateTime(1)
    val dtEnd = dtStart.plusMillis(6)
    val dr = DateTimeRange(dtStart, dtEnd)
    val intervals = dr.intervals(2).toList

    intervals.size shouldEqual(4)
    intervals(0) shouldEqual(DateTimeRange(dtStart, dtStart.plusMillis(1)))
    intervals(1) shouldEqual(DateTimeRange(dtStart.plusMillis(2), dtStart.plusMillis(3)))
    intervals(2) shouldEqual(DateTimeRange(dtStart.plusMillis(4), dtStart.plusMillis(5)))
    intervals(3) shouldEqual(DateTimeRange(dtStart.plusMillis(6), dtStart.plusMillis(6)))
  }
}

