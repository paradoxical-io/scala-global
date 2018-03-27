package io.paradoxical.common.streams

object Streams {
  /**
   * Create a lazy infinite stream from the call by name provider.
   *
   * If the sequence is empty the stream will end.
   *
   * @param data
   * @tparam T
   * @return
   */
  def fromSeq[T](data: => Seq[T]): Stream[T] = {
    fromOpt({
      val x = data

      if (x.isEmpty) {
        None
      } else {
        Some(x)
      }
    }).flatten
  }

  /**
   * Create a lazy infinite stream from the call by name provider
   *
   * When a None is encountered the stream will end
   *
   * @param data
   * @tparam T
   * @return
   */
  def fromOpt[T](data: => Option[T]): Stream[T] = {
    new Iterator[T] {
      var n = data

      override def hasNext = {
        n.isDefined
      }

      override def next() = {
        val current = n.get

        n = data

        current
      }
    }.toStream
  }
}
