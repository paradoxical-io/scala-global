package io.paradoxical.common.extensions

import io.paradoxical.common.date.{DateTimePlus, LocalDatePlus}
import io.paradoxical.common.execution._
import io.paradoxical.common.string.{RichStrings, StringToNumber}
import org.joda.time._
import scala.collection.parallel.{ParIterable, ParIterableLike}
import scala.concurrent._

object Extensions extends Extensions

class Extensions {
  implicit def mapToFlattenableMap[S, T](ori: Map[S, Option[T]]): FlattenableMap[S, T] = {
    new FlattenableMap[S, T](ori)
  }

  implicit def mapToMapWithMapValuesNow[S, T](ori: Map[S, T]): MapWithMapValuesNow[S, T] = {
    new MapWithMapValuesNow[S, T](ori)
  }

  implicit def mapToFlipFloppableMap[S, T](ori: Map[S, Set[T]]): FlipFloppableMap[S, T] = {
    new FlipFloppableMap[S, T](ori)
  }

  implicit def iterableToSafelySummableIterable[R](ori: Iterable[R]): SafelySummableIterable[R] = {
    new SafelySummableIterable[R](ori)
  }

  implicit def iterableToPageableIterable[T](ori: Iterable[T]): PageableIterable[T] = {
    new PageableIterable[T](ori)
  }

  implicit def optionToOptionWithThrow[T](o: Option[T]): OptionWithThrow[T] = {
    new OptionWithThrow[T](o)
  }

  implicit def parIterableToConfigurableParIterable[T](par: ParIterable[T]): ConfigurableParIterable[T] = {
    new ConfigurableParIterable(par)
  }

  implicit def dateTimeToDateTimePlus(dt: DateTime): DateTimePlus = {
    new DateTimePlus(dt)
  }

  implicit def localDateToLocalDatePlus(ld: LocalDate): LocalDatePlus = {
    new LocalDatePlus(ld)
  }

  implicit def string2StringToNumber(str: String): StringToNumber = {
    new StringToNumber(str)
  }

  implicit def string2PimpedString(str: String): RichStrings = {
    new RichStrings(str)
  }

  implicit def throwable2ThrowablePlus(t: Throwable): ThrowablePlus = {
    new ThrowablePlus(t)
  }

  implicit def optionalTuple2Plus[S, T](ori: Option[(S, T)]): RichTuples[S, T] = {
    new RichTuples[S, T](ori)
  }

  implicit def iterableToIterableForGroupableMap[S, T](original: Iterable[(S, T)]): IterableForGroupableMap[S, T] = {
    new IterableForGroupableMap(original)
  }

  implicit def iterableToIterableWithTailOption[T](original: Iterable[T]): IterableWithTailOption[T] = {
    new IterableWithTailOption(original)
  }

  implicit def iterableToIterableWithSeqOption[T](original: Iterable[T]): IterableWithSeqOption[T] = {
    new IterableWithSeqOption(original)
  }

  implicit def iterableToCustomDistinctIterable[T](ori: Iterable[T]): CustomDistinctIterable[T] = {
    new CustomDistinctIterable[T](ori)
  }

  implicit def futureToAwaitableFuture[T](f: Future[T]): AwaitableFuture[T] = {
    new AwaitableFuture[T](f)
  }

  implicit def futureToMaxWaitFuture[T](f: Future[T]): MaxWaitFuture[T] = {
    new MaxWaitFuture[T](f)
  }

  implicit def futureToJavaToScalaFuture[T](f: java.util.concurrent.Future[T]): JavaToScalaFuture[T] = {
    new JavaToScalaFuture[T](f)
  }

  implicit def javaFutureToScalaFuture[T](f: Future[T]): ScalaToJavaFuture[T] = {
    new ScalaToJavaFuture[T](f)
  }

  implicit def extendParallelizable[Y <: ParIterableLike[_, _, _]](par: Y): ParallelizableExtensions[Y] = {
    new ParallelizableExtensions(par)
  }

  implicit def objectToAppliableFunctionObject[T](original: T): AppliableFunctionObject[T] = {
    new AppliableFunctionObject(original)
  }

  implicit def productToRichProduct[T <: Product](original: T): RichProduct[T] = {
    new RichProduct(original)
  }

  implicit def optionProductToRichOptionProduct[T <: Product](original: Option[T]): RichOptionProduct[T] = {
    new RichOptionProduct(original)
  }

  implicit def nullableToRichNullable[T](original: T): RichNullable[T] = {
    new RichNullable[T](original)
  }
}
