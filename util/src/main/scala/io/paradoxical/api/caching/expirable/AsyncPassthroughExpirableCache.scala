package io.paradoxical.api.caching.expirable

import io.paradoxical.api.caching.CacheApi
import io.paradoxical.common.types.Id
import scala.concurrent.duration.FiniteDuration
import scala.concurrent.{ExecutionContext, Future}

/**
 * Cache that does not cache
 *
 * @tparam T       Key Type
 * @tparam Y       Value Type
 */
class AsyncPassthroughExpirableCache[T, Y] extends ExpirableCache[T, Y, Future] {
  override def getOrSetWithTtl(key: T, ttl: Option[FiniteDuration])(value: => Y): Future[Y] = {
    Future.successful(value)
  }

  override def exists(key: T) = {
    Future.successful(false)
  }

  override def setWithTtl(data: ExpirableCacheableItem[T, Y]): Future[Y] = {
    Future.successful(data.value)
  }

  override def get(key: T): Future[Option[Y]] = {
    Future.successful(None)
  }

  override def getOrSet(key: T, value: => Y): Future[Y] = {
    Future.successful(value)
  }

  override def set(key: T, value: Y): Future[Y] = {
    Future.successful(value)
  }

  override def invalidate(keys: T*): Future[Unit] = {
    Future.successful(Unit)
  }

  override def invalidateAll(): Future[Unit] = {
    Future.successful(Unit)
  }

  override def getAll(keys: T*): Future[Map[T, Y]] = {
    Future.successful(Map.empty)
  }

  override def getAndLazySetAsync(key: T, value: => Future[Y])(implicit executionContext: ExecutionContext): Future[Option[Y]] = {
    value.map(Some(_))
  }
}


/**
 * Cache that does not cache
 *
 * @tparam T       Key Type
 * @tparam Y       Value Type
 */
class SyncPassthroughExpirableCache[T, Y] extends ExpirableCache[T, Y, Id] {
  override def getOrSetWithTtl(key: T, ttl: Option[FiniteDuration])(value: => Y): Y = {
    value
  }

  override def exists(key: T) = {
    false
  }

  override def setWithTtl(data: ExpirableCacheableItem[T, Y]): Y = {
    data.value
  }

  override def get(key: T): Option[Y] = {
    None
  }

  override def getOrSet(key: T, value: => Y): Y = {
    value
  }

  override def set(key: T, value: Y): Y = {
    value
  }

  override def invalidate(keys: T*): Unit = {}

  override def invalidateAll(): Unit = {}

  override def getAll(keys: T*): Map[T, Y] = Map.empty

  override def getAndLazySetAsync(key: T, value: => Future[Y])(implicit executionContext: ExecutionContext): Option[Y] = {
    None
  }
}
