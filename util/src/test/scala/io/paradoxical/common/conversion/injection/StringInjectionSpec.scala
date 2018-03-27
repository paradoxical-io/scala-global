//
// StringInjectionSpec.scala
package io.paradoxical.common.conversion.injection

import org.scalatest.{FlatSpec, Matchers}
import Injection._
import java.net.{URL, URLEncoder}
import java.util.UUID
import scala.util.Success

class StringInjectionSpec extends FlatSpec with Matchers {
  "StringInjections" should "convert a URL to a string" in {
    val urlStr = "http://google.com"
    val url = new URL(urlStr)

    urlToString(url) should equal(urlStr)
    // Inversions return Trys
    urlToString.invert(urlStr) should equal(Success(url))

    val invalidUrlString = "sldkfjsdf"
    val urlTry = urlToString.invert(invalidUrlString)
    urlTry.isFailure shouldBe true
    an[UnsupportedOperationException] should be thrownBy urlTry.get
  }

  it should "convert a UUID to a String" in {
    val uuid = UUID.randomUUID()

    uuidToString(uuid) should equal(uuid.toString)
    uuidToString.invert(uuid.toString) should equal(Success(uuid))

    val notAUuid = "klsjfdsldf"
    val uuidTry = uuidToString.invert(notAUuid)
    uuidTry.isFailure shouldBe true
    an[UnsupportedOperationException] should be thrownBy uuidTry.get
  }

  it should "convert a string to a URLEncodedString" in {
    val url = new URL("http://google.com")
    val urlString = url.toString
    val encodedUrlString = URLEncoder.encode(urlString, "UTF-8")

    stringToUrlEncodedString(urlString) should equal(URLEncodedString(encodedUrlString))
    stringToUrlEncodedString.invert(URLEncodedString(encodedUrlString)) should equal(Success(urlString))
  }
}
