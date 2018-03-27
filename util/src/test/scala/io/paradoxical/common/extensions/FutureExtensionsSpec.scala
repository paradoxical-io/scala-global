package io.paradoxical.common.extensions

import io.paradoxical.common.execution.FutureUtils
import org.scalatest.{FlatSpec, Inside, Inspectors, Matchers}
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Promise
import scala.util.{Failure, Success}

class FutureExtensionsSpec extends FlatSpec with Matchers with Inside with Inspectors {
  "Future.select()" should "return the first result" in {

    def tryBothForIndex(i: Int) = {
      def success = {
        val fs = (0 until 10 map { _ => Promise[Int] }) toArray
        val f = FutureUtils.select(fs.map(_.future))
        f.isCompleted shouldBe false
        fs(i).trySuccess(1)
        Thread.sleep(100)
        f.isCompleted shouldBe true
        inside(f.value.get) {
          case Success((Success(1), rest)) =>
            rest should have size 9
            val elems = fs.slice(0, i) ++ fs.slice(i + 1, 10)
            rest should contain theSameElementsAs elems
            true
        }
      }

      def failure = {
        val fs = (0 until 10 map { _ => Promise[Int] }) toArray
        val f = FutureUtils.select(fs.map(_.future))
        f.isCompleted shouldBe false
        val e = new Exception("sad panda")
        fs(i).tryFailure(e)
        Thread.sleep(100)
        f.isCompleted shouldBe true
        inside (f.value.get) {
          case Success((Failure(e), rest)) =>
            rest should have size 9
            val elems = fs.slice(0, i) ++ fs.slice(i + 1, 10)
            rest should contain theSameElementsAs elems
            true
          case Failure(e) =>
            fail()
        }
      }

      success
      failure
    }

    // Ensure this works for all indices:
    0 until 10 foreach { tryBothForIndex }
  }
}
