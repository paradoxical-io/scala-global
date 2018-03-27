//
// BinaryBijections.scala
package io.paradoxical.common.conversion.bijection

import java.util.Base64

case class Base64String(str: String) extends AnyVal

trait BinaryBijections {
  implicit def unboxBase64String(b: Base64String): String = b.str
  implicit def boxBase64String(s: String): Base64String = Base64String(s)

  private lazy val base54Encoder = Base64.getEncoder
  private lazy val base64Decoder = Base64.getDecoder

  /**
   * Bijection between byte array and Base64 encoded string.
   *
   * The "trim" here is important, as encodeBase64String sometimes
   * tags a newline on the end of its encoding. DON'T REMOVE THIS
   * CALL TO TRIM.
   */
  implicit lazy val bytesToBase64: Bijection[Array[Byte], Base64String] =
    new AbstractBijection[Array[Byte], Base64String] {
      def apply(bytes: Array[Byte]) = Base64String(base54Encoder.encodeToString(bytes).trim)
      override def invert(b64: Base64String): Array[Byte] = base64Decoder.decode(b64.str)
    }
}
