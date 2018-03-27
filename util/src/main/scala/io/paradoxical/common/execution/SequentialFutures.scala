package io.paradoxical.common.execution

import scala.collection.generic.CanBuildFrom
import scala.concurrent.{ExecutionContext, Future}

class SequentialFutures {
  /**
   * Serialize futures sequentially.  Given a collection, for each element in the collection
   * run the future. When the future completes, execute the next future
   *
   * @param collection The source collection
   * @param fn         The function to apply each element against
   * @param ec         The execution context
   * @param cbf        The implicit builder to translate from one collection to annother
   * @tparam Element
   * @tparam OtherElement
   * @tparam Collection
   * @return
   */
  @deprecated("Please use the companion object static methods", since = "2.2.x")
  def serialize[Element, OtherElement, Collection[Element] <: Iterable[Element]](
    collection: Collection[Element]
  )(fn: Element => Future[OtherElement])(
    implicit ec: ExecutionContext,
    cbf: CanBuildFrom[Collection[OtherElement], OtherElement, Collection[OtherElement]]
  ): Future[Collection[OtherElement]] = {
    SequentialFutures.serialize(collection)(fn)
  }
}

object SequentialFutures {
  /**
   * Serialize futures sequentially.  Given a collection, for each element in the collection
   * run the future. When the future completes, execute the next future
   *
   * @param collection The source collection
   * @param fn         The function to apply each element against
   * @param ec         The execution context
   * @param cbf        The implicit builder to translate from one collection to annother
   * @tparam Element
   * @tparam OtherElement
   * @tparam Collection
   * @return
   */
  def serialize[Element, OtherElement, Collection[Element] <: Iterable[Element]](
    collection: Collection[Element]
  )(fn: Element => Future[OtherElement])(
    implicit ec: ExecutionContext,
    cbf: CanBuildFrom[Collection[OtherElement], OtherElement, Collection[OtherElement]]
  ): Future[Collection[OtherElement]] = {
    val builder = cbf()
    builder.sizeHint(collection.size)

    collection.foldLeft(Future(builder)) {
      (previousFuture, next) =>
        for {
          previousResults <- previousFuture
          next <- fn(next)
        } yield previousResults += next
    } map { builder => builder.result }
  }

  /**
   * Serialize futures sequentially in groups applying the function to each element.
   *
   * Given a collection, for each group of elements in the collection
   * run the future against each element in parallel.
   * When the group of future completes, execute the next future group
   *
   * @param collection The source collection
   * @param batchSize  The batch size
   * @param fn         The function to apply each element against
   * @param ec         The execution context
   * @tparam Element
   * @tparam OtherElement
   * @return
   */
  def serializeBatched[Element, OtherElement](
    collection: Iterable[Element],
    batchSize: Int
  )(fn: Element => Future[OtherElement])(
    implicit ec: ExecutionContext
  ): Future[Iterable[OtherElement]] = {

    collection.grouped(batchSize).foldLeft(Future(List.empty[OtherElement])) {
      (previousFuture, next) =>
        for {
          previousResults <- previousFuture

          next <- Future.sequence(next.map(fn))
        } yield {
          previousResults ++ next
        }
    }
  }

  /**
   * Serialize futures sequentially groups applying the function to groups of elements.
   *
   * Given a collection, for each group of elements in the collection
   * run the future against each group in parallel.
   *
   * When the group of future completes, execute the next future group
   *
   * @param collection The source collection
   * @param batchSize  The batch size
   * @param fn         The function to apply each element against
   * @param ec         The execution context
   * @tparam Element
   * @tparam OtherElement
   * @return
   */
  def batched[Element, OtherElement](
    collection: Iterable[Element],
    batchSize: Int
  )(fn: Iterable[Element] => Future[Iterable[OtherElement]])(
    implicit ec: ExecutionContext
  ): Future[List[OtherElement]] = {

    collection.grouped(batchSize).foldLeft(Future(List.empty[OtherElement])) {
      (previousFuture, next) =>
        for {
          previousResults <- previousFuture

          next <- fn(next)
        } yield {
          previousResults ++ next
        }
    }
  }
}
