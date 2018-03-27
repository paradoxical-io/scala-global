package io.paradoxical.common.sampling

import io.paradoxical.common.extensions.Extensions._
import org.scalatest.{FlatSpec, Matchers}
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class SamplerPathTests extends FlatSpec with Matchers {
  "Path sampler" should "return a and execute b if sampled" in {
    val sample = new SampledPath(new Sampler(1.0))

    var executed = false

    sample.sample(1, 2).get(Some((a, b) => {
      executed = true
    })) shouldEqual 1

    executed shouldEqual true
  }

  it should "return a and not execute b if not sampled" in {
    val sample = new SampledPath(new Sampler(0.0))

    var executed = false

    sample.sample(1, 2).get(Some((a, b) => {
      executed = true
    })) shouldEqual 1

    executed shouldEqual false
  }

  "Async path sampler" should "return a and not execute b if not sampled" in {
    val sample = new SampledPath(new Sampler(0.0))

    var executed = false

    sample.sampleAsync(
      Future.successful(1),
      Future.successful(2)
    ).get(Some((a, b) => {
      executed = true
    })).waitForResult() shouldEqual 1

    executed shouldEqual false
  }

  it should "return a and execute b if sampled" in {
    val sample = new SampledPath(new Sampler(1.0))

    var executed = false

    sample.sampleAsync(
      Future.successful(1),
      Future.successful(2)
    ).get(Some((a, b) => {
      executed = true
    })).waitForResult() shouldEqual 1

    executed shouldEqual true
  }

  it should "not fail the request if b is sampled and b fails" in {
    val sample = new SampledPath(new Sampler(1.0))

    var executed = false

    sample.sampleAsync(
      Future.successful(1),
      Future.failed[Int](new RuntimeException("B Failed"))
    ).get(Some((a, _) => {
      executed = true
    })).waitForResult() shouldEqual 1

    executed shouldEqual false
  }
}
