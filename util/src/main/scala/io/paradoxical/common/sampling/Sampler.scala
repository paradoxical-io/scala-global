package io.paradoxical.common.sampling

import scala.util.Random

/**
 * Even distribution sampling.  Choose a rate from 0 to 1 (with 2 digit precision)
 *
 * @param rate Percentage to allow through. From 0 to 1.
 */
class Sampler(@volatile private var rate: Double) {
  verify(rate)

  private def verify(d: Double): Unit = {
    require(d >= 0.0 && d <= 1.0, s"Sample rate of $d should be between 0 and 1")
  }

  private val rand = new Random()

  def setRate(sampleRate: Double): Unit = {
    verify(sampleRate)

    rate = sampleRate
  }

  def check(): Boolean = {
    // random from 0 to 1000 representing .1% granularity from 0 to 1
    // if the sample rate is .05 the hit value is going to be 50
    // leveraging a uniform distribution of random we should get an int
    // value between 0 and 50 5% of the time (50/1000 = 0.05)
    val hitValue = Math.floor(rate * 1000)

    hitValue > rand.nextInt(1000)
  }
}
