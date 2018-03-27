package io.paradoxical.common.execution

import com.google.common.util.concurrent.{FutureCallback, Futures, ListenableFuture}
import io.paradoxical.macros.v1.SourceContext
import java.util.concurrent.{ScheduledExecutorService, TimeUnit, TimeoutException, Future => JFuture}
import scala.concurrent._
import scala.concurrent.duration.{Duration, FiniteDuration}
import scala.util.Try

class AwaitableFuture[T](f: Future[T]) {
  def waitUntil(duration: Duration = Duration.Inf): T = {
    Await.result(f, duration)
  }

  def waitForResult(): T = {
    waitUntil(Duration.Inf)
  }
}

class MaxWaitFuture[T](f: Future[T]) {

  /**
   * Provides an asynchronous extension method for timing out a future. The result can be recovered.
   *
   * @param maxWait The timeout duration.
   * @param scheduledExecutorService The executor that the timeout failure runs on. That operation is basically instantaneous, so in most cases
   *                                 this should be a single threaded pool. ex.
   *                                 implicit val scheduledExecutor = ExecutionContextProvider.provider.of(Executors.newSingleThreadScheduledExecutor())
   * @note This function does not provide a strict future timeout, as it's quite possible that the future has already started executing.
   *       Instead, this function starts a timer on the future. After this function is called, if that timer exceeds the
   *       atMost duration and the original future has not completed, the future will resolve with a TimeoutException.
   * @note IMPORTANT: This does not cancel the underlying future. It will still be running on it's own context until it completes.
   *       This simply provides a non-blocking equivalent to Await.result.
   */
  def withMaxWait(maxWait: FiniteDuration)(implicit scheduledExecutorService: ScheduledExecutorService): Future[T] = {
    val p = Promise[T]()
    p.tryCompleteWith(f)

    // Create the timeout future and schedule it
    lazy val error = new TimeoutException(s"Future timed out after ${maxWait.toSeconds} seconds")
    val action = new Runnable {override def run(): Unit = p.tryFailure(error)}
    scheduledExecutorService.schedule(action, maxWait.toMillis, TimeUnit.MILLISECONDS)

    p.future
  }

  /**
   * Provides an asynchronous extension method for timing out a future and provides contextual information about
   * where the Future originated. The result can be recovered.
   *
   * @param maxWait The timeout duration.
   * @param scheduledExecutorService The executor that the timeout failure runs on. That operation is basically instantaneous, so in most cases
   *                                 this should be a single threaded pool. ex.
   *                                 implicit val scheduledExecutor = ExecutionContextProvider.provider.of(Executors.newSingleThreadScheduledExecutor())
   * @note This function does not provide a strict future timeout, as it's quite possible that the future has already started executing.
   *       Instead, this function starts a timer on the future. After this function is called, if that timer exceeds the
   *       atMost duration and the original future has not completed, the future will resolve with a TimeoutException.
   * @note IMPORTANT: This does not cancel the underlying future. It will still be running on it's own context until it completes.
   *       This simply provides a non-blocking equivalent to Await.result.
   */
  def withContextualMaxWait(maxWait: FiniteDuration)(implicit scheduledExecutorService: ScheduledExecutorService, sourceContext: SourceContext): Future[T] = {
    val p = Promise[T]()
    p.tryCompleteWith(f)

    // Create the timeout future and schedule it
    lazy val error = new TimeoutException(s"Future initiated from file: ${sourceContext.fileName}, line ${sourceContext.line} timed out after ${maxWait.toSeconds} seconds")
    val action = new Runnable {override def run(): Unit = p.tryFailure(error)}
    scheduledExecutorService.schedule(action, maxWait.toMillis, TimeUnit.MILLISECONDS)

    p.future
  }

  /**
    * Provides an asynchronous extension method for timing out a future. Recovers to a defaultValue on timeout.
    *
    * @param maxWait The timeout duration.
    * @param scheduledExecutorService The executor that the timeout failure runs on. That operation is basically instantaneous, so in most cases
    *                                 this should be a single threaded pool. ex.
    *                                 implicit val scheduledExecutor = ExecutionContextProvider.provider.of(Executors.newSingleThreadScheduledExecutor())
    * @param defaultValue The default to return if the future times out.
    * @note This function does not provide a strict future timeout, as it's quite possible that the future has already started executing.
    *       Instead, this function starts a timer on the future. After this function is called, if that timer exceeds the
    *       atMost duration and the original future has not completed, the future will resolve with a TimeoutException.
    * @note IMPORTANT: This does not cancel the underlying future. It will still be running on it's own context until it completes.
    *       This simply provides a non-blocking equivalent to Await.result.
    */
  def withMaxWaitAndDefault(
    maxWait: FiniteDuration,
    defaultValue: T
  )(implicit executionContext: ExecutionContext, scheduledExecutorService: ScheduledExecutorService): Future[T] = {
    withMaxWait(maxWait).recover {
      case _: TimeoutException => defaultValue
    }
  }

    /**
    * Provides an asynchronous extension method for timing out a future. and provides contextual information about
    * where the Future originated. Recovers to a defaultValue on timeout.
    *
    * @param maxWait The timeout duration.
    * @param scheduledExecutorService The executor that the timeout failure runs on. That operation is basically instantaneous, so in most cases
    *                                 this should be a single threaded pool. ex.
    *                                 implicit val scheduledExecutor = ExecutionContextProvider.provider.of(Executors.newSingleThreadScheduledExecutor())
    * @param defaultValue The default to return if the future times out.
    * @note This function does not provide a strict future timeout, as it's quite possible that the future has already started executing.
    *       Instead, this function starts a timer on the future. After this function is called, if that timer exceeds the
    *       atMost duration and the original future has not completed, the future will resolve with a TimeoutException.
    * @note IMPORTANT: This does not cancel the underlying future. It will still be running on it's own context until it completes.
    *       This simply provides a non-blocking equivalent to Await.result.
    */
  def withContextualMaxWaitAndDefault(
    maxWait: FiniteDuration,
    defaultValue: T
  )(implicit executionContext: ExecutionContext, scheduledExecutorService: ScheduledExecutorService, sourceContext: SourceContext): Future[T] = {
    withContextualMaxWait(maxWait).recover {
      case _: TimeoutException => defaultValue
    }
  }
}

/**
 * Marker trait to know if we converted a scala future to a java one
 *
 * @tparam T
 */
trait ScalaConvertedFuture[T] {
  def originalFuture: Future[T]
}

class ScalaToJavaFuture[T](future: Future[T]) {
  def toJavaFuture: JFuture[T] = {
    new JFuture[T] with ScalaConvertedFuture[T] {
      override def isCancelled: Boolean = throw new UnsupportedOperationException

      override def get(): T = Await.result(future, Duration.Inf)

      override def get(timeout: Long, unit: TimeUnit): T = Await.result(future, Duration.create(timeout, unit))

      override def cancel(mayInterruptIfRunning: Boolean): Boolean = throw new UnsupportedOperationException

      override def isDone: Boolean = future.isCompleted

      override def originalFuture: Future[T] = future
    }
  }
}

class JavaToScalaFuture[T](f: JFuture[T]) {
  def toScalaFuture(): Future[T] = {
    f match {
      case lf: ListenableFuture[T] => {
        val p = Promise[T]
        Futures.addCallback(lf, new FutureCallback[T]() {
          def onSuccess(t: T): Unit = {
            p.success(t)
          }

          def onFailure(t: Throwable): Unit = p.failure(t)
        })
        p.future
      }

      // if the java future is actually a scala converted future, return the raw future
      // bypassing the extra thread creation
      case p: ScalaConvertedFuture[T] => {
        p.originalFuture
      }

      case p: JFuture[T] => {
        val wrappedPromise = Promise[T]()
        new Thread(new Runnable {
          override def run(): Unit = {
            wrappedPromise.complete(Try {
              p.get
            })
          }
        }).start()

        wrappedPromise.future
      }

      case _ => throw new IllegalArgumentException("Only instances of ListenableFuture may be transformed to scala futures.")
    }
  }
}
