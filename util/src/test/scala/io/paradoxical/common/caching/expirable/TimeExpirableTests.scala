package io.paradoxical.common.caching.expirable

import org.joda.time.DateTime
import org.scalatest.{FlatSpec, Matchers}
import scala.concurrent.duration._

class TimeExpirableTests extends FlatSpec with Matchers {
  "Time based expiration" should "expire" in {
    var data = 0L

    var now = new DateTime(0)

    val expirable = new TimeBasedExpirable[Long](50 milli, now = () => now)({
      data = data + 1
      data
    })

    expirable.get shouldEqual 1

    now = now.plusMillis(25)

    // verify its cached
    expirable.get shouldEqual 1

    now = now.plusMillis(30)

    // verify it reset
    expirable.get shouldEqual 2
  }

  "TIme based executor" should "expire" in {
    var data = 0L

    var now = new DateTime(0)

    def method(x: Unit): Long = {
      data = data + 1

      data
    }

    val expirable = new TimeBasedExecutionLimiter(50 milli, now = () => now)(method)

    data shouldEqual 0

    expirable.sample(Unit) shouldEqual 1

    data shouldEqual 1

    now = now.plusMillis(25)

    // verify its not executed since the ttl hasn't expired yet
    expirable.sample(Unit) shouldEqual 1

    data shouldEqual 1

    now = now.plusMillis(30)

    // verify it reset
    expirable.sample(Unit) shouldEqual 2

    data shouldEqual 2
  }
}
