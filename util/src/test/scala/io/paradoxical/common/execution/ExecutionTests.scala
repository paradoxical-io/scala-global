package io.paradoxical.common.execution

import org.scalatest.{FlatSpec, Matchers}
import scala.concurrent.{Await, Future}
import scala.concurrent.duration._
import scala.concurrent.ExecutionContext.Implicits.global

class ExecutionTests extends FlatSpec with Matchers {
  "Futures" should "serialize" in {
    val start = System.currentTimeMillis
    val doubled = Await.result({
      SequentialFutures.serialize(List(10, 20)) { i =>
        Future {
          Thread.sleep(i)
          i * 2
        }
      }
    }, 1 second)

    val timeElapsed = System.currentTimeMillis - start
    timeElapsed should be >= (30l)
    doubled should be(List(20, 40))
  }

  it should "serialize in groups" in {
    val start = System.currentTimeMillis
    val doubled = Await.result({
      SequentialFutures.serializeBatched(List(10, 20, 30), batchSize = 2) { i =>
        Future {
          Thread.sleep(i)
          i * 2
        }
      }
    }, 1 second)

    val timeElapsed = System.currentTimeMillis - start
    timeElapsed should be >= 30L
    doubled should be(List(20, 40, 60))
  }

  it should "serialize in batches" in {
    val start = System.currentTimeMillis
    val doubled = Await.result({
      SequentialFutures.batched(List(10, 20, 30), batchSize = 3) { i =>
        Future {
          Thread.sleep(i.sum)
          i.map(_ * 2)
        }
      }
    }, 1 second)

    val timeElapsed = System.currentTimeMillis - start
    timeElapsed should be >= 60l
    doubled should be(List(20, 40, 60))
  }
}
