//
// StatusMonitor.scala
package io.paradoxical.common.streams

import org.slf4j.LoggerFactory

/**
 * A helpful utility for printing the status of processing a large collection.
 * To use it, import the implicits and add a `.withStatusLogging` line.
 * It will print each time an item is pulled from the collection.
 */
object StatusMonitor {
  private val logger = LoggerFactory.getLogger(getClass)
  private val MILLIS_IN_MINUTES = 1000 * 60

  def withStatusLogging[T](col: Iterator[T], updateEvery: Int = 1, totalSize: Option[Long] = None): Iterator[T] = {
    val startTime = System.currentTimeMillis()
    col.zipWithIndex.map {
      case (item, idx) => {
        val numProcessed = idx + 1
        if (numProcessed % updateEvery == 0) {
          val elapsed = System.currentTimeMillis() - startTime
          val average = elapsed.toDouble / numProcessed.toDouble
          val etr = totalSize.map(total => {
            val numMillisLeft = ((total - numProcessed) * average).toLong
            s" ETR: $numMillisLeft milliseconds (${(numMillisLeft / MILLIS_IN_MINUTES).toInt} minutes)."
          })
          logger.info(s"Processed $numProcessed${totalSize.map(" / " + _).getOrElse("")} items in $elapsed milliseconds; $average milliseconds per item." + etr.getOrElse(""))
        }
        item
      }
    }
  }

  implicit def iteratorToPimped[T](it: Iterator[T]): PimpedIterator[T] = {
    new PimpedIterator(it)
  }

  implicit def iterableToPimped[T](it: Iterable[T]): PimpedIterable[T] = {
    new PimpedIterable(it)
  }

  protected class PimpedIterator[T](source: Iterator[T]) {
    def withStatusLogging(updateEvery: Int = 1, totalSize: Option[Long] = None): Iterator[T] = {
      StatusMonitor.withStatusLogging(source, updateEvery, totalSize)
    }
  }

  protected class PimpedIterable[T](source: Iterable[T]) {
    def withStatusLogging(updateEvery: Int = 1): Iterator[T] = {
      StatusMonitor.withStatusLogging(source.toIterator, updateEvery, Some(source.size))
    }
  }
}
