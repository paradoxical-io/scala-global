package io.paradoxical.common.caching

import io.paradoxical.api.caching.expirable.{ExpirableCache, ExpirableCacheableItem}
import scala.concurrent.{ExecutionContext, Future}

object CacheExtensions {
  object Implicits {
    implicit class AsyncCacheExtensions[T, Y](val cache: ExpirableCache[T, Y, Future]) {
      protected val logger = org.slf4j.LoggerFactory.getLogger(getClass)

      /**
       * Batch get and set with ttl. If the cache FAILS the fill function will be called to populate the data
       *
       * @param keys The keys to fetch
       * @param fill A fill function that takes the cache _misses_ and returns how to resolve the values. The values are set in the cache
       * @param executionContext
       * @return The requested result of key -> value map
       */
      def batchGetOrSetWithTtl(keys: Set[T])
        (fill: (Set[T]) => Future[Set[ExpirableCacheableItem[T, Y]]])
        (implicit executionContext: ExecutionContext): Future[Map[T, Y]] = {
        cache.getAll(keys.toSeq: _*).flatMap(hits => {
          val keysFound = hits.keys.toSet

          val keysToFill = keys -- keysFound

          if (keysToFill.isEmpty) {
            Future.successful(hits)
          } else {
            for {
              newItems <- fill(keysToFill)

              _ <- Future.sequence(newItems.map(cache.setWithTtl))
            } yield {
              toMap(newItems) ++ hits
            }
          }
        }).recoverWith {
          case ex: Exception =>
            logger.warn("Unable to retrieve or set from cache! Delegating to fill function to return fresh data anyways", ex)

            fill(keys).map(toMap)
        }
      }

      private def toMap(items: Set[ExpirableCacheableItem[T, Y]]): Map[T, Y] = {
        items.map(item => item.key -> item.value).toMap
      }
    }
  }
}