package io.paradoxical.config.api.v1

import scala.concurrent.duration.Duration

/**
 * Generates a reloadable config that is a projection from another reloadable config
 *
 * Useful for defining interfaces for common configuration types
 *
 * @param reloadableConfig The original reloadable config
 * @param f The projection function
 * @tparam T The original reloadable config type
 * @tparam U The new reloadable config type
 */
class ProjectedReloadableConfig[T, U](reloadableConfig: ReloadableConfig[T])(f: T => U) extends ReloadableConfig[U] {
  override def watch(duration: Duration)(onChange: U => Unit): WatchToken = reloadableConfig.watch(duration) { t => onChange(f(t)) }

  override def currentValue() = f(reloadableConfig.currentValue())
}