package io.paradoxical.config.api.v1

import scala.concurrent.duration.Duration

/**
 * A static config that does not change
 *
 * @param data
 * @tparam T
 */
case class StaticConfig[T](data: T) extends ReloadableConfig[T] {
  override def watch(duration: Duration)(onChange: T => Unit): WatchToken = {
    new WatchToken(None)
  }

  override def currentValue(): T = data
}