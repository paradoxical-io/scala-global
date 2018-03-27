package io.paradoxical.common.extensions

class RichTuples[S, T](ori: Option[(S, T)]) {
  def explode: (Option[S], Option[T]) = (ori.map(_._1), ori.map(_._2))
}
