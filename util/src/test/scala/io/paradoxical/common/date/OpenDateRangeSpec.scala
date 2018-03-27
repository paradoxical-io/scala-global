
package io.paradoxical.common.date

import org.joda.time.LocalDate
import org.scalatest.{FlatSpec, Matchers}

class OpenDateRangeSpec extends FlatSpec with Matchers {

  val d1 = new LocalDate("2015-01-01")
  val d2 = new LocalDate("2015-02-01")

  "An OpenDateRange should be able to parse" should ": as (None, None)" in {
    val range = OpenDateRange.parse(":")
    range.isDefined shouldEqual true

    val realRange = range.get

    realRange.toString shouldEqual ":"
    realRange.hasStart shouldEqual false
    realRange.hasEnd shouldEqual false
  }

  it should "2015-01-01: as (LocalDate(2015-01-01), None)" in {
    val range = OpenDateRange.parse("2015-01-01:")
    range.isDefined shouldEqual true

    val realRange = range.get

    realRange.toString shouldEqual "2015-01-01:"
    realRange.hasStart shouldEqual true
    realRange.hasEnd shouldEqual false
  }

  it should ":2015-01-01 as (None, LocalDate(2015-01-01))" in {
    val range = OpenDateRange.parse(":2015-01-01")
    range.isDefined shouldEqual true

    val realRange = range.get

    realRange.toString shouldEqual ":2015-01-01"
    realRange.hasStart shouldEqual false
    realRange.hasEnd shouldEqual true
  }

  it should "2015-01-01:2015-02-01 as (LocalDate(2015-01-01), LocalDate(2015-02-01))" in {
    val range = OpenDateRange.parse("2015-01-01:2015-02-01")
    range.isDefined shouldEqual true

    val realRange = range.get

    realRange.toString shouldEqual "2015-01-01:2015-02-01"
    realRange.hasStart shouldEqual true
    realRange.hasEnd shouldEqual true
  }
}
