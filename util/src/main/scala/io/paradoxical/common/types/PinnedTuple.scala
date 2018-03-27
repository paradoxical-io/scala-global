package io.paradoxical.common.types

trait PinnedTupleType[T] extends Product2[String, T] {
  def unparse: String
  def toTuple: (String, T) = (_1, _2)
}

/**
 * Simple trait to make it easy to create context keys that are just strings
 */
trait PinnedStringTuple extends PinnedTuple {
  override type Value = String

  override def parse(r: String): String = r
}

/**
 * A tuple with the key pinned
 */
trait PinnedTuple {
  self =>
  val key: String

  /**
   * The custom type of this key
   */
  type Value

  /**
   * A tuple of (String, Value)
   */
  type Key = PinnedTupleType[Value]

  /**
   * Utility to allow the container to provide a mapping from Value => String
   *
   * @param r
   * @return
   */
  def parse(r: String): Value

  def unparse(v: Value): String = v.toString

  def apply(data: Value): Key = new Key {
    override def _1: String = key

    override def _2: Value = data

    /**
     * Allow a mapping of Value => String
     *
     * @return
     */
    override def unparse: String = self.unparse(data)

    override def equals(obj: scala.Any): Boolean = {
      canEqual(obj)
    }

    override def canEqual(that: Any): Boolean = {
      that != null &&
      that.isInstanceOf[PinnedTupleType[_]] &&
      that.asInstanceOf[PinnedTupleType[_]]._1 == key &&
      that.asInstanceOf[PinnedTupleType[_]]._2 == data
    }
  }
}

