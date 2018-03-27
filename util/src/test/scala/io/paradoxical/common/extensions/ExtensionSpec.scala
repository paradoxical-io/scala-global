package io.paradoxical.common.extensions

import io.paradoxical.common.extensions.Extensions._
import com.google.common.util.concurrent.SettableFuture
import java.util.concurrent.{CompletableFuture, ScheduledThreadPoolExecutor, TimeoutException, Future => JFuture}
import org.scalatest._
import org.slf4j.LoggerFactory
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._
import scala.concurrent.{Await, Future, Promise}

class ExtensionSpec extends FlatSpec with Matchers {
  private val logger = LoggerFactory.getLogger(getClass)

  "Rich iterable" should "page" in {
    val iterable = List(1, 2, 3, 4, 5)

    iterable.skipLimit(offset = 1, limit = 2).toList shouldEqual List(2, 3)
  }

  it should "bypass paging if limit is negative" in {
    val iterable = List(1, 2, 3, 4, 5)

    iterable.skipLimit(offset = 0, limit = -1).toList shouldEqual iterable
  }

  "Java futures" should "be listenable" in {
    val f: JFuture[String] = CompletableFuture.completedFuture("foo")

    f.toScalaFuture().waitForResult() shouldEqual "foo"
  }

  "Scala futures" should "convert to java futures" in {
    Future(1).toJavaFuture.toScalaFuture().waitForResult() shouldEqual 1
  }

  it should "time out with withMaxWait" in {
    implicit val scheduledExecutor = new ScheduledThreadPoolExecutor(2)
    val testString = "Test"
    def fut: Future[String] = Promise[String]().future

    Await.result(
      fut.withMaxWait(20 millis).recover {
        case e: TimeoutException =>
          logger.error("Timeout", e)
          testString
      },
      5 seconds
    ) shouldEqual testString

    Await.result(
      fut.withMaxWaitAndDefault(20 millis, testString),
      5 seconds
    ) shouldEqual testString
  }

  "Guava futures" should "be listenable" in {
    val guavaFuture = SettableFuture.create[String]()

    guavaFuture.set("foo")

    guavaFuture.toScalaFuture().waitForResult() shouldEqual "foo"
  }

  "Maps" should "flip a map" in {
    val map = Map(1 -> Set("one"), 2 -> Set("two"), 3 -> Set("three"), 4 -> Set("three"))

    map.flip shouldEqual Map("one" -> Set(1), "two" -> Set(2), "three" -> Set(3, 4))
  }

  "Option" should "throw if empty" in {
    intercept[Exception] {
      None.getOrThrow(new RuntimeException)
    }
  }

  it should "get if not empty" in {
    Some(1).getOrThrow(new RuntimeException) shouldEqual 1
  }

  "String" should "trim to option" in {
    "".trimToOption shouldEqual None

    " ".trimToOption shouldEqual None

    "\t".trimToOption shouldEqual None

    ("" +
     "").trimToOption shouldEqual None

    val nString: String = null
    nString.trimToOption shouldEqual None

    "abc".trimToOption shouldEqual Some("abc")
    "abc   ".trimToOption shouldEqual Some("abc")
    "abc \t".trimToOption shouldEqual Some("abc")
  }

  "Function applier" should "apply conditionally" in {
    val s = "update"
    "".applyIf(true)(_.concat(s)) shouldEqual s
    "".applyIf(false)(_.concat(s)) shouldEqual ""
  }

  it should "apply optional value" in {
    val s = "update"
    "".applyOptionalValue(Some("update"))(_.concat(_)) shouldEqual s
    "".applyOptionalValue[String](None)(_.concat(_)) shouldEqual ""
  }

  "Products" should "check for all fields being None" in {
    AllOptions(None, None).allFieldsAreNone shouldBe true
    AllOptions(None, Some(5L)).allFieldsAreNone shouldBe false
    SomeOptions("", None).allFieldsAreNone shouldBe false
  }

  "Option Products" should "convert to None" in {
    Some(AllOptions(None, None)).orNoneIfAllFieldsAreNone shouldEqual None
    Some(AllOptions(None, Some(5L))).orNoneIfAllFieldsAreNone shouldEqual Some(AllOptions(None, Some(5L)))
    Some(SomeOptions("", None)).orNoneIfAllFieldsAreNone shouldEqual Some(SomeOptions("", None))
    None.orNoneIfAllFieldsAreNone shouldBe None
  }

  "Nullable" should "coalesce to value" in {
    val nulled: String = null
    val notNulled: String = "s2"

    notNulled ?? "test" shouldEqual notNulled
    nulled ?? "test" shouldEqual "test"
  }
}

case class AllOptions(a: Option[String], b: Option[java.lang.Long])
case class SomeOptions(a: String, b: Option[java.lang.Long])
