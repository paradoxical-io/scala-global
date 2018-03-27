package io.paradoxical.common.sampling

import scala.concurrent.{ExecutionContext, Future}

/**
 * Run codepaths through a sampler
 *
 * @param sampler
 */
class SampledPath(val sampler: Sampler) {
  protected val logger = org.slf4j.LoggerFactory.getLogger(getClass)

  class SyncSample[A, B](a: => A, b: => B) {
    def get(verify: Option[(A, B) => Unit] = None): A = {
      val aResult = a

      if (sampler.check()) {
        verify.foreach(f => f(aResult, b))
      }

      aResult
    }
  }

  class AsyncSample[A, B](a: => Future[A], b: => Future[B]) {
    def get(verify: Option[(A, B) => Unit] = None)(implicit executionContext: ExecutionContext): Future[A] = {
      // set both futures in motion

      val aFuture = a

      val bFuture = if (sampler.check()) {
        // make sure that if B fails
        // we don't fail the composed future
        b.map(Some(_)).recover {
          case e: Exception =>
            logger.warn("Unable to sample B request", e)
            None
        }
      } else {
        Future.successful(None)
      }

      for {
        aResult <- aFuture

        bResult <- bFuture
      } yield {
        bResult.foreach(b => {
          verify.foreach(f => f(aResult, b))
        })

        aResult
      }
    }
  }

  /**
   * Sample sync
   *
   * @param a          Always run A function
   * @param b          Sample for B function
   * @tparam T type of A return
   * @tparam Y type of B return
   * @return
   */
  def sample[T, Y](a: => T, b: => Y): SyncSample[T, Y] = new SyncSample(a, b)

  /**
   * Samples Async
   *
   * @param a          Always run the A function
   * @param b          Sample to run the B function
   * @return
   */
  def sampleAsync[T, Y](
    a: => Future[T],
    b: => Future[Y]
  ): AsyncSample[T, Y] = new AsyncSample(a, b)
}
