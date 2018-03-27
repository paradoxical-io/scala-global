package io.paradoxical.common.sampling

import org.scalatest.{FlatSpec, Matchers}

class SamplerTests extends FlatSpec with Matchers {
  trait SampleVerify {
    def rate: Double

    val tolerancePercent: Double = .01

    val sampler = new Sampler(rate)

    val max = 10000000

    // sample N times and count the number of times the check was true
    val sampled = (0 until max).map(_ => sampler.check()).count(identity)

    // we should have rate * max samples +- 1% to allow for deviation
    val expectedToBeSampled = (max * rate).toInt

    def onePercentTolerance(total: Int) = (total * tolerancePercent).toInt
  }

  "Sampler" should "sample" in new SampleVerify {
    override def rate = .05

    assert(expectedToBeSampled === sampled +- onePercentTolerance(max))
  }

  it should "never sample" in new SampleVerify {
    override def rate = .0

    override val tolerancePercent: Double = 0.0

    assert(0 === sampled)
  }

  it should "always sample" in new SampleVerify {
    override def rate = 1.0

    override val tolerancePercent: Double = 0.0

    assert(max === sampled)
  }

  it should "allow sample changes" in {
    def onePercentTolerance(total: Int) = (total * 0.01).toInt

    val rate = 0.05
    val sampler = new Sampler(rate)

    val max = 10000000

    def doSample = (0 until max).map(_ => sampler.check()).count(identity)

    assert((max * rate).toInt === doSample +- onePercentTolerance(max))

    sampler.setRate(0.0)

    assert(0 === doSample)
  }
}
