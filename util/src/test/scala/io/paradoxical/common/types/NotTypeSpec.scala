package io.paradoxical.common.types

import org.scalatest.{FlatSpec, Matchers}

class NotTypeTest extends NotTypeImplicits {
  def isNotType[T : Manifest: Not[Long]#Evidence](): Boolean = manifest[T].runtimeClass != classOf[Long]
}

class SomethingTest {
  def isSomething[T : Manifest : NotNothing](): Boolean = manifest[T].runtimeClass != classOf[Nothing]
}

class NotUnitTest {
  def isNotUnit[T : Manifest : NotUnit](): Boolean = manifest[T].runtimeClass != classOf[Unit]
}

class NotUnitNotNothingTest {
  def isNotUnitOrNothing[T : Manifest : NotUnit : NotNothing](): Boolean =
    manifest[T].runtimeClass != classOf[Unit] && manifest[T].runtimeClass != classOf[Nothing]
}

class NotTypeSpec extends FlatSpec with Matchers with NotTypeImplicits {
  "Not type" should "allow" in {
    val holder = new NotTypeTest

    assert(holder.isNotType[String]())
  }

  it should "enforce" in {
    "new NotTypeTest().isNotType[Long]()" shouldNot compile
  }

  "Not nothing" should "allow" in {
    val holder = new SomethingTest

    assert(holder.isSomething[String]())
  }

  it should "enforce" in {
    "new SomethingTest().isSomething()" shouldNot compile
  }

  "Not unit" should "allow" in {
    val holder = new NotUnitTest

    assert(holder.isNotUnit[String]())
  }

  it should "enforce" in {
    "new NotUnitTest().isNotUnit[Unit]()" shouldNot compile
  }

  "Not unit or nothing" should "allow" in {
    val holder = new NotUnitNotNothingTest

    assert(holder.isNotUnitOrNothing[String]())
  }

  it should "enforce for unit" in {
    "new NotUnitOrNothingTest().isNotUnitOrNothing[Unit]()" shouldNot compile
  }

  it should "enforce for nothing" in {
    "new NotUnitOrNothingTest().isNotUnitOrNothing()" shouldNot compile
  }
}
