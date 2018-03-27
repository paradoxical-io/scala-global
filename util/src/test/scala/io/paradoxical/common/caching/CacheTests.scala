package io.paradoxical.common.caching

import java.util.concurrent.CountDownLatch
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.{Future, blocking}
import org.scalatest.{FlatSpec, Matchers}

class CacheTests extends FlatSpec with Matchers {
  "Cache" should "cache" in {
    val cache = new InMemoryCache[Int, Int]()

    assert(cache.getOrSet(1, 1) == 1)

    assert(cache.get(1) == Some(1))
  }

  it should "invalidate a key" in {
    val cache = new InMemoryCache[Int, Int]()

    cache.set(1, 1)

    assert(cache.get(1) == Some(1))

    cache.invalidate(1)

    assert(cache.get(1).isEmpty)
  }

  it should "invalidate all keys" in {
    val cache = new InMemoryCache[Int, Int]()

    cache.set(1, 1)
    cache.set(2, 2)

    assert(cache.get(1) == Some(1))
    assert(cache.get(2) == Some(2))

    cache.invalidateAll()

    assert(cache.get(1).isEmpty)
    assert(cache.get(2).isEmpty)
  }

  it should "lazy load with a future" in {
    val cache = new InMemoryCache[Int, Int]()

    val latch = new CountDownLatch(1)

    val result =
      cache.getAndLazySetAsync(1, Future {
        blocking {
          latch.await()
          1
        }
      })

    assert(result.isEmpty)

    latch.countDown()

    Thread.sleep(100)

    assert(cache.get(1).isDefined)
  }

  it should "get all values from the cache" in {
    val cache = new InMemoryCache[Int, Int]()

    cache.set(1, 1)
    cache.set(2, 2)

    cache.getAll(1, 2, 3, 4) shouldEqual Map (
      1 -> 1,
      2 -> 2
    )

    cache.invalidateAll()

    cache.getAll(1, 2, 3, 4) shouldEqual Map.empty
  }
}
