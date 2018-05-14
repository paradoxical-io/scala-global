package io.paradoxical.config.api.v1

import java.util.concurrent.ScheduledFuture

/**
 * Wrapper to cancel scheduled futures
 *
 * @param scheduledFuture
 */
class WatchToken(scheduledFuture: Option[ScheduledFuture[_]]) {
  def cancel(): Unit = scheduledFuture.map(_.cancel(true))
}