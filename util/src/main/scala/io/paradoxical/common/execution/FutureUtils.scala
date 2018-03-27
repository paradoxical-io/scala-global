package io.paradoxical.common.execution

import scala.concurrent.{ExecutionContext, Future, Promise}
import scala.util.Try

object FutureUtils {
  /**
   * Given a sequence of Futures, return a tuple containing the first Future to complete's result in the first element
   * and the remaining outstanding Futures in the second element
   *
   * @param fs
   * @param executionContext
   * @tparam A
   * @return
   */
  def select[A](fs: Seq[Future[A]])(implicit executionContext: ExecutionContext): Future[(Try[A], Seq[Future[A]])] = {
    if (fs.isEmpty) {
      Future.failed(new IllegalArgumentException("empty future list"))
    } else {
      val p = Promise[(Try[A], Seq[Future[A]])]()
      val as = fs.map(f => Promise().tryCompleteWith(f) -> f)

      as.foreach {
        case (a, f) =>
          a.future.onComplete(t => {
            if (!p.isCompleted) {
              val filtered = as.filter(_._2 ne f).map(_._2)
              p.tryCompleteWith(Future.successful(t -> filtered))
            }
          })
      }

      p.future
    }
  }
}
