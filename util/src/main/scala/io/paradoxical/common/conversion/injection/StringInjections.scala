//
// StringInjections.scala
package io.paradoxical.common.conversion.injection

import io.paradoxical.common.conversion.injection.Inversion._
import java.net.{URL, URLDecoder, URLEncoder}
import java.util.UUID
import scala.util.Try

trait StringInjections {
  implicit val stringToLong: Injection[Long, String] = new AbstractInjection[Long, String] {
    override def apply(a: Long): String = a.toString
    override def invert(b: String): Try[Long] = attempt(b)(_.toLong)
  }

  implicit val urlToString: Injection[URL, String] = new AbstractInjection[URL, String] {
    override def apply(a: URL): String = a.toString
    override def invert(b: String): Try[URL] = attempt(b)(new URL(_))
  }

  implicit val uuidToString: Injection[UUID, String] = new AbstractInjection[UUID, String] {
    override def apply(a: UUID): String = a.toString
    override def invert(b: String): Try[UUID] = attempt(b)(UUID.fromString)
  }

  implicit val stringToUrlEncodedString: Injection[String, URLEncodedString] =
    new AbstractInjection[String, URLEncodedString] {
      override def apply(a: String): URLEncodedString = URLEncodedString(URLEncoder.encode(a, "UTF-8"))
      override def invert(b: URLEncodedString): Try[String] = attempt(b)(s => URLDecoder.decode(s.encodedString, "UTF-8"))
    }
}

case class URLEncodedString(encodedString: String) extends AnyVal

