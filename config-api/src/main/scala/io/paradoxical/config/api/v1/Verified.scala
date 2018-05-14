package io.paradoxical.config.api.v1

/**
 * A configuration companion object can be marked as Verified and will be executed
 *
 * before the config is loaded. This lets configurations define system level pre-checks before they can be loaded
 */
trait Verified {
  def verify(): Unit
}