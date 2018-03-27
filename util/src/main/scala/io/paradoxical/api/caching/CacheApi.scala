package io.paradoxical.api.caching

import scala.concurrent.{ExecutionContext, Future}

/**
 * A cache interface
 *
 * @tparam T       Key Type
 * @tparam Y       Value Type
 * @tparam Wrapper Return type (Future, Option, CacheApi.Id)
 */

trait CacheApi[T, Y, Wrapper[_]] {
  def get(key: T): Wrapper[Option[Y]]

  def getOrSet(key: T, value: => Y): Wrapper[Y]

  def set(key: T, value: Y): Wrapper[Y]

  def invalidate(keys: T*): Wrapper[Unit]

  def invalidateAll(): Wrapper[Unit]

  def getAll(keys: T*): Wrapper[Map[T, Y]]

  def getAndLazySetAsync(key: T, value: => Future[Y])(implicit executionContext: ExecutionContext): Wrapper[Option[Y]]

  def exists(key: T): Wrapper[Boolean]
}