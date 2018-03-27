package io.paradoxical.common.execution

import scala.concurrent.{ExecutionContext, ExecutionContextExecutorService}
import java.util.concurrent.{ AbstractExecutorService, TimeUnit }
import java.util.Collections

// stolen from victor klang: https://gist.github.com/viktorklang/5245161
/**
 * Convert an execution context into an execution context executor service
 */
object ExecutionContextExecutorServiceBridge {
  def apply(ec: ExecutionContext): ExecutionContextExecutorService = ec match {
    case null => throw null
    case eces: ExecutionContextExecutorService => eces
    case other => new AbstractExecutorService with ExecutionContextExecutorService {
      override def prepare(): ExecutionContext = other
      override def isShutdown = false
      override def isTerminated = false
      override def shutdown() = ()
      override def shutdownNow() = Collections.emptyList[Runnable]
      override def execute(runnable: Runnable): Unit = other execute runnable
      override def reportFailure(t: Throwable): Unit = other reportFailure t
      override def awaitTermination(length: Long,unit: TimeUnit): Boolean = false
    }
  }
}
