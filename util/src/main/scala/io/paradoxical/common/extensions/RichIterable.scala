package io.paradoxical.common.extensions

import scala.collection.parallel.{ParIterable, TaskSupport}

class ConfigurableParIterable[T](par: ParIterable[T]) {
  def withTaskSupport(tasksupport: TaskSupport): ParIterable[T] = {
    par.tasksupport = tasksupport
    par
  }
}

class FlattenableMap[S, T](m: Map[S, Option[T]]) {
  def flattenMap: Map[S, T] = {
    m.flatMap(entry => {
      val key = entry._1
      entry._2.map(value => (key, value))
    })
  }
}

class MapWithMapValuesNow[S, T](m: Map[S, T]) {
  /**
   * Maps a maps values without using a view,
   *
   * @param f
   * @tparam U
   * @return
   */
  def mapValuesNow[U](f: T => U): Map[S, U] = {
    m.map {
      case (s, t) => (s, f(t))
    }
  }
}

class SafelySummableIterable[R](it: Iterable[R]) {
  def safeAvg[B >: R](implicit num: Numeric[B]): B = {
    if (it.isEmpty) {
      num.zero
    } else {
      num match {
        case frac: Fractional[_] => import frac._; it.sum(num) / fromInt(it.size)
        case inte: Integral[_] => import inte._; it.sum(num) / fromInt(it.size)
        case _ => sys.error("Undivisable numeric!")
      }
    }
  }

  def safeSum[B >: R](implicit num: Numeric[B]): B = {
    if (it.isEmpty) {
      num.zero
    } else {
      it.sum(num)
    }
  }

  def safeMax[B >: R](implicit cmp: Ordering[B]): Option[R] = {
    if (it.isEmpty) {
      None
    } else {
      Some(it.max(cmp))
    }
  }

  def safeMin[B >: R](implicit cmp: Ordering[B]): Option[R] = {
    if (it.isEmpty) None else Some(it.min(cmp))
  }

  def sumOption[B >: R](implicit num: Numeric[B]): Option[B] = {
    if (it.isEmpty) {
      None
    } else {
      Some(it.sum(num))
    }
  }
}

class PageableIterable[T](it: Iterable[T]) {
  /**
   * Simulate a paging effect on an iterable
   *
   * WARNING: iterables are NOT guaranteed to have O(1) skip access and you may end up
   * traversing the entire iterable to get the paging effect. Please be careful!
   *
   * @param offset
   * @param limit
   */
  def skipLimit(offset: Int, limit: Int): Iterable[T] = {
    if (limit < 0) {
      it.drop(offset)
    } else {
      it.slice(offset, offset + limit)
    }
  }
}

class CustomDistinctIterable[T](it: Iterable[T]) {
  def distinctWith[K](f: (T) => K): Iterable[T] = {
    it.groupBy(f).map(k => k._2.head)
  }
}

class FlipFloppableMap[S, T](m: Map[S, Set[T]]) {
  def flip: Map[T, Set[S]] = {
    m.toList.flatMap(e => e._2.map(b => (e._1, b))).map(_.swap).groupBy(_._1).mapValues(b => b.map(_._2).toSet)
  }
}

class IterableForGroupableMap[S, T](t: Iterable[(S, T)]) {
  def toGroupedMap: Map[S, Iterable[T]] = {
    t.groupBy(_._1).mapValues(_.map(_._2))
  }
}

class IterableWithTailOption[T](it: Iterable[T]) {
  def tailOption: Option[Iterable[T]] = {
    if (it.isEmpty) {
      None
    } else {
      Some(it.tail)
    }
  }
}

class IterableWithSeqOption[T](it: Iterable[T]) {
  def seqOption: Option[Iterable[T]] = {
    if (it.nonEmpty) {
      Some(it)
    } else {
      None
    }
  }
}
