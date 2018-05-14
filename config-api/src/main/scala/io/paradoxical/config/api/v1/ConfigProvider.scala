package io.paradoxical.config.api.v1

/**
 * A generic configuration provider
 *
 * @tparam T
 */
trait ConfigProvider[+T] {
  def currentValue(): T
}