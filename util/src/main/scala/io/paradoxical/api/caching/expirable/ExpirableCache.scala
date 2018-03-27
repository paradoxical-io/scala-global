package io.paradoxical.api.caching.expirable

import io.paradoxical.api.caching.CacheApi
import scala.concurrent.duration.FiniteDuration

case class ExpirableCacheableItem[T, Y](key: T, value: Y, ttl: Option[FiniteDuration])

/**
 * Reflects a cache that supports TTL setting
 *
 * @tparam T       Key Type
 * @tparam Y       Value Type
 * @tparam Wrapper Return type (Future, Option, CacheApi.Id)
 */
trait ExpirableCache[T, Y, Wrapper[_]] extends CacheApi[T, Y, Wrapper] {
  def getOrSetWithTtl(key: T, ttl: Option[FiniteDuration] = None)(value: => Y): Wrapper[Y]

  def setWithTtl(data: ExpirableCacheableItem[T, Y]): Wrapper[Y]
}