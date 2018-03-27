package io.paradoxical.common.execution

import java.util.concurrent._
import java.util.concurrent.atomic.AtomicInteger
import scala.concurrent.duration.{FiniteDuration, _}

/**
 * Create threads as background daemons, useful for schedulers
 *
 * @param name
 */
@Deprecated // use named thread factory directly
class DaemonThreadFactory(name: String) extends NamedThreadFactory(name, true)

class NamedThreadFactory(name: String, isDaemon: Boolean = true) extends ThreadFactory {
  private val next = new AtomicInteger(1)

  override def newThread(r: Runnable): Thread = {
    val t = new Thread(r)
    t.setDaemon(isDaemon)
    t.setName(name + "-" + next.getAndAdd(1))
    t
  }
}

class CachedThreadpool(
  maxThreads: Int,
  namedThreadFactory: NamedThreadFactory,
  timeout: FiniteDuration = 60 seconds
) {
  lazy val get = {
    new ThreadPoolExecutor(0,
      maxThreads,
      timeout.toSeconds,
      TimeUnit.SECONDS,
      new SynchronousQueue[Runnable],
      namedThreadFactory)
  }
}