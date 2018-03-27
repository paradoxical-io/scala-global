package io.paradoxical.common.reflection

import java.lang.annotation.Annotation
import java.lang.reflect.{AnnotatedElement, Field}

case class AnnotationData(field: Field, position: Int)

object AnnotationReader {
  def getOptionalAnnotation[T <: Annotation](ae: AnnotatedElement, annoClazz: Class[T]): Option[T] = {
    Option(ae.getDeclaredAnnotation(annoClazz))
  }

  def getRequiredAnnotation[T <: Annotation](clazz: AnnotatedElement, annoClazz: Class[T]): T = {
    getOptionalAnnotation[T](clazz, annoClazz).getOrElse(throw new IllegalArgumentException)
  }

  /**
   * Find an annotation on the field and get the position in the field array that had the annotation
   * @param fields
   * @param clazz
   * @tparam T
   * @return
   */
  def findOptionalFieldWithAnnotation[T <: Annotation](fields: Array[Field], clazz: Class[T]): Option[AnnotationData] = {
    fields.zipWithIndex.find {
      case (f, _) => getOptionalAnnotation[T](f, clazz).isDefined
    }.map {
      case (field, index) => AnnotationData(field, index)
    }
  }

  def findFieldWithAnnotation[T <: Annotation](fields: Array[Field], clazz: Class[T]): AnnotationData = {
    findOptionalFieldWithAnnotation[T](fields, clazz).getOrElse(throw new IllegalArgumentException)
  }
}
