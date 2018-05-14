package io.paradoxical.config.api.v1

import scala.concurrent.duration._

/**
 * A reloadable config can be reloaded at runtime
 *
 * @tparam T
 */
trait ReloadableConfig[+T] extends ConfigProvider[T] {
  def map[U](f: T => U): ReloadableConfig[U] = new ProjectedReloadableConfig[T, U](this)(f)

  def watch(duration: Duration = 1 second)(onChange: T => Unit): WatchToken
}