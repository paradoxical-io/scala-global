package io.paradoxical.common.caching.expirable

import org.joda.time.{DateTime, DateTimeZone}
import scala.concurrent.duration.FiniteDuration

class Expirable[T, Ctx](
  isExpired: Ctx => Boolean,
  onSet: Option[Ctx] => Option[Ctx]
)(seed: => T) {
  private var data: Option[T] = None

  private var _lastCtx: Option[Ctx] = None

  def get: T = {
    synchronized {
      val expired = _lastCtx.exists(isExpired)

      if (data.isEmpty || expired) {
        data = Some(seed)

        _lastCtx = onSet(_lastCtx)
      }

      data.get
    }
  }
}

/**
 * Thread safe accessing of an expirable variable
 *
 * @param expires
 * @param seed
 * @param now Function to provide the current time
 * @tparam T
 */
class TimeBasedExpirable[T](
  expires: FiniteDuration,
  now: () => DateTime = () => DateTime.now(DateTimeZone.UTC)
)(seed: => T) extends Expirable[T, DateTime](
  isExpired = nextExpirationTime => {
    now().isAfter(nextExpirationTime.toInstant)
  },
  onSet = _ => {
    Some(now().plusMillis(expires.toMillis.toInt))
  }
)(seed)


/**
 * Thread safe limitation of a call by name method
 *
 * Only applies the execute method _if_ the time has expired, then resets.
 *
 * This allows for easy debouncing of frequently called methods where it is OK to
 * drop the input data.
 *
 * Good usecases here are things like heartbeats and checkpointers.  You may not care
 * if it runs every 1 ms, as long as it runs with the latest data every 10 seconds. This lets
 * you write code such that its checkpointed _each_ time but
 *
 * For example:
 *
 * {{{
 *   def checkpoint(data: String) { ... }
 *
 *   val checkpointer = new TimeBasedExecutionLimiter(30 seconds)(checkpoint)
 *
 *   while(true) {
 *     checkpointer.sample(...some-data...)
 *   }
 * }}}
 *
 * @param expires A call by name provider of the duration. This lets you modify durations
 *                using reloadable configs
 * @param now     An optional method that provides the current time (used for testing)
 * @tparam T The input type to the method to debounce
 * @tparam Y the output type of the method
 */
class TimeBasedExecutionLimiter[T, Y](
  expires: => FiniteDuration,
  now: () => DateTime = () => DateTime.now(DateTimeZone.UTC)
)(method: T => Y) {

  case class TimeResult(lastExecuted: DateTime, lastData: Y)

  private var _lastCtx: Option[TimeResult] = None

  private def isExpired(nextExpirationTime: DateTime): Boolean = {
    now().isAfter(nextExpirationTime.toInstant)
  }

  /**
   * Executes the cached method with the argument if the last time the method was run is past the expiration
   * If its never been run will run once.
   *
   * @param data
   * @return The last valid result of an execution.
   */
  def sample(data: T): Y = {
    synchronized {
      val expired = _lastCtx.isEmpty || _lastCtx.exists(c => isExpired(c.lastExecuted))

      if (expired) {
        val result = method(data)

        val executionTime = now().plusMillis(expires.toMillis.toInt)

        _lastCtx = Some(TimeResult(executionTime, result))
      }
    }

    _lastCtx.map(_.lastData).getOrElse(throw new RuntimeException("Execution limiter should have had data but does not!"))
  }
}