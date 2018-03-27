package io.paradoxical.common.caching

import com.google.common.cache.CacheBuilder
import io.paradoxical.api.caching.CacheApi
import io.paradoxical.common.types.Id
import java.util.concurrent.{Callable, TimeUnit}
import scala.collection.JavaConverters._
import scala.concurrent.duration.FiniteDuration
import scala.concurrent.{ExecutionContext, Future}

/**
 * To support caching value types, we need to auto box them
 *
 * Otherwise guava caches do not support caching primitives (or AnyVals)
 *
 * @param value
 * @tparam T
 */
case class CacheBox[T](value: T)

class InMemoryCache[T, Y](
  maxSize: Option[Int] = None,
  expireAfterRead: Option[FiniteDuration] = None,
  expireAfterWrite: Option[FiniteDuration] = None
) extends CacheApi[T, Y, Id] {

  protected def cacheBuilder = {
    val builder = CacheBuilder.newBuilder().recordStats()

    maxSize.foreach(builder.maximumSize(_))
    expireAfterRead.foreach(m => builder.expireAfterAccess(m.toMillis, TimeUnit.MILLISECONDS))
    expireAfterWrite.foreach(m => builder.expireAfterWrite(m.toMillis, TimeUnit.MILLISECONDS))

    builder
  }

  lazy val cache = {
    cacheBuilder.build[CacheBox[T], CacheBox[Y]]
  }

  def get(key: T): Option[Y] = {
    Option(cache.getIfPresent(toBox(key))).map(_.value)
  }

  /**
   * Do not use this if the "body" makes async calls that rely on other threads for completion (e.g. Await)
   * that may also be using the cache. It is safer to use getAndLazySetAsync (with the caveat that multiple
   * cache fills might be happening in parallel).
   */
  def getOrSet(key: T, value: => Y): Y = {
    cache.get(key, new Callable[CacheBox[Y]] {
      override def call(): CacheBox[Y] = value
    })
  }

  def set(key: T, value: Y): Y = {
    cache.put(key, value)

    value
  }

  override def exists(key: T): Boolean = {
    get(key).isDefined
  }

  def invalidate(keys: T*): Unit = {
    cache.invalidateAll(keys.map(toBox).asJava)
  }

  def invalidateAll(): Unit = {
    cache.invalidateAll()
  }

  /**
   * Non-blocking get from the current cache and seed the cache IF the
   * the current value is missing. This prevents blocking inside the cache
   *
   * @param key
   * @param value
   * @return
   */
  def getAndLazySetAsync(key: T, value: => Future[Y])(implicit executionContext: ExecutionContext): Option[Y] = {
    val existingValue = get(key)

    if (existingValue.isEmpty) {
      value.foreach(t => {
        cache.put(key, t)
      })
    }

    existingValue
  }

  override def getAll(keys: T*): Id[Map[T, Y]] = {
    cache.getAllPresent(keys.map(toBox).asJava).asScala.map {
      case (key, value) => fromBox(key) -> fromBox(value)
    }.toMap
  }

  private implicit def toBox[X](t: X): CacheBox[X] = CacheBox(t)

  private implicit def fromBox[X](cacheBox: CacheBox[X]): X = cacheBox.value
}
