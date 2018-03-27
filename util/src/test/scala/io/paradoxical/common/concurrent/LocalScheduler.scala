package io.paradoxical.common.concurrent

import java.util
import java.util.logging.Logger
import scala.collection.mutable

/**
 * Super-simplified version of Twitter's LocalScheduler
 */
object LocalScheduler {

  val log: Logger = Logger.getLogger(getClass.getSimpleName)

  /**
   * Super-simplified version of Twitter's Activation
   */
  class Activation extends Iterator[Runnable] {

    private[this] val rs = new util.ArrayDeque[Runnable]
    private[this] var running = false

    override def toString: String = s"Activation(${System.identityHashCode(this)})"

    def submit(r: Runnable): Unit = {
      assert(r != null)
      rs.addLast(r)
      if (!running) run()
    }

    def flush(): Unit = if (running) run()

    @inline def hasNext: Boolean = running && !rs.isEmpty

    @inline def next(): Runnable = {
      if (rs.isEmpty) null else rs.removeFirst()
    }

    private[this] def run(): Unit = {
      val save = running
      running = true
      try {
        while (hasNext) next().run()
      } finally {
        running = save
      }
    }
  }
}

/**
 * Super-simplified version of Twitter's LocalScheduler
 */
class LocalScheduler {
  import LocalScheduler.Activation

  // use weak refs to prevent Activations from causing a memory leak
  // thread-safety provided by synchronizing on `activations`
  private[this] val activations = new mutable.WeakHashMap[Activation, Boolean]()

  private[this] val local = new ThreadLocal[Activation]()

  override def toString: String = s"LocalScheduler(${System.identityHashCode(this)})"

  private[this] def get(): Activation = {
    val a = local.get()
    if (a != null)
      return a

    val activation = new Activation()
    local.set(activation)
    activations.synchronized {
      activations.put(activation, java.lang.Boolean.TRUE)
    }
    activation
  }

  /** An implementation of Iterator over runnable tasks */
  @inline def hasNext: Boolean = get().hasNext

  /** An implementation of Iterator over runnable tasks */
  @inline def next(): Runnable = get().next()

  // Scheduler implementation:
  def submit(r: Runnable): Unit = get().submit(r)
  def flush(): Unit = get().flush()
}
