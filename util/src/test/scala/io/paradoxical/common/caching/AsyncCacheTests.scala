package io.paradoxical.common.caching

import io.paradoxical.api.caching.expirable.{AsyncPassthroughExpirableCache, ExpirableCacheableItem}
import io.paradoxical.common.caching.CacheExtensions.Implicits._
import io.paradoxical.common.extensions.Extensions._
import org.mockito.Matchers.{eq => meq, _}
import org.mockito.Mockito._
import org.scalatest.mockito.MockitoSugar
import org.scalatest.{FlatSpec, Matchers}
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.util.control.NoStackTrace

case class TestError() extends Exception with NoStackTrace

class AsyncCacheTests extends FlatSpec with Matchers with MockitoSugar {
  "Async cache extensions" should "batch get or set" in {
    val cache = new AsyncPassthroughExpirableCache[String, String]

    val results =
      cache.batchGetOrSetWithTtl(Set("foo")) { keys =>
        Future.sequence(
          keys.map(key => {
            Future.successful(ExpirableCacheableItem(key, "bar", ttl = None))
          }))
      }.waitForResult()

    results shouldEqual Map(
      "foo" -> "bar"
    )
  }

  it should "call the fill function if the cache fails" in {
    val cache = spy(new AsyncPassthroughExpirableCache[String, String])

    when(cache.getAll(anyVararg[String])).thenReturn(Future.failed(TestError()))

    val results =
      cache.batchGetOrSetWithTtl(Set("foo")) { keys =>
        Future.sequence(
          keys.map(key => {
            Future.successful(ExpirableCacheableItem(key, "bar", ttl = None))
          }))
      }.waitForResult()

    results shouldEqual Map(
      "foo" -> "bar"
    )
  }

  it should "not call the fill if all the keys are in the cache" in {
    val cache = spy(new AsyncPassthroughExpirableCache[String, String])

    when(cache.getAll(anyVararg[String])).thenReturn(Future.successful(
      Map(
        "existing" -> "already exists"
      )
    ))

    val results =
      cache.batchGetOrSetWithTtl(Set("existing")) { keys =>
        Future.sequence(
          keys.map(key => {
            Future.successful(ExpirableCacheableItem(key, "bar", ttl = None))
          }))
      }.waitForResult()

    results shouldEqual Map(
      "existing" -> "already exists"
    )

    // because there was no new value, don't set anything in the cache
    verify(cache, times(0)).setWithTtl(any[ExpirableCacheableItem[String, String]]())
  }

  it should "request keys to the fill that were not already in the cache" in {
    val cache = spy(new AsyncPassthroughExpirableCache[String, String])

    when(cache.getAll(anyVararg[String])).thenReturn(Future.successful(
      Map(
        "existing" -> "already exists"
      )
    ))

    val newItem = "missing" -> "bar"

    val results =
      cache.batchGetOrSetWithTtl(Set("existing", "missing")) { keys =>
        Future.sequence(
          keys.map(key => {
            Future.successful(ExpirableCacheableItem(key, "bar", ttl = None))
          }))
      }.waitForResult()

    results shouldEqual Map(
      "existing" -> "already exists",
      newItem
    )

    // make sure we set the value in the caches
    verify(cache, times(1)).setWithTtl(meq(ExpirableCacheableItem(newItem._1, newItem._2, ttl = None)))
  }
}
