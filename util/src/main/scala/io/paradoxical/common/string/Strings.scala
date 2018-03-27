package io.paradoxical.common.string

import java.net._

object Strings {

  // Pattern used to operate on unix-style paths
  private val UNIX_PATH_SEPARATOR = "/"
  private val UNIX_PATH_SEPARATOR_REGEX = "/+".r

  /**
    * Combine path separators (e.g. for converting "//a/b////c" to "/a/b/c")
    */
  def combinePathSeparators(path: String): String = {
    UNIX_PATH_SEPARATOR_REGEX.replaceAllIn(path, UNIX_PATH_SEPARATOR)
  }

  /**
   * Converts a String to an Option[String].
   *
   * If the String is null or empty then this method will return
   * an Empty option.
   *
   */
  def trimToOption: (String => Option[String]) = {
    Option(_).flatMap(s => if (s.trim.isEmpty) None else Some(s.trim))
  }

  /**
   * A function equivalent to javascript's encodeURIComponent.
   * Stolen from http://stackoverflow.com/questions/607176/java-equivalent-to-javascripts-encodeuricomponent-that-produces-identical-outpu
   */
  def encodeURIComponent(s: String): String = {
    URLEncoder.encode(s, "UTF-8").
      replaceAll("\\+", "%20").
      replaceAll("\\%21", "!").
      replaceAll("\\%27", "'").
      replaceAll("\\%28", "(").
      replaceAll("\\%29", ")").
      replaceAll("\\%7E", "~")
  }

  def decodeURIComponent(s: String): String = {
    val rd = s.replaceAll("\\%20", "+").
      replaceAll("\\!", "%21").
      replaceAll("\\'", "%27").
      replaceAll("\\(", "%28").
      replaceAll("\\)", "%29").
      replaceAll("\\~", "%7E")
    URLDecoder.decode(rd, "UTF-8")
  }

  /**
   * Attempts to call decodeURIComponent on a string and will simply return the input
   * string if the call fails.
   */
  def safeDecodeURIComponent(s: String): String = {
    try {
      decodeURIComponent(s)
    } catch {
      case e: Exception => s
    }
  }

  /**
   * Often, clients give us poorly formatted urls.
   * This function tries to replace unsafe characters with their encoded counterparts.
   * This does not include reserved characters (such as '&' or '/').
   */
  def encodeUnsafeCharacters(url: String): String = {
    try {
      // We want to preserve any % that are part of a %HH code, but encode all others.
      // Match any '%' where at least one of the next 2 characters is non-hex.
      val preProcessedUrl = """%(([^0-9a-fA-F])|([0-9a-fA-F][^0-9a-fA-F]))""".r replaceAllIn(url, "%25$1")
      val u = new URL(preProcessedUrl)
      val uri = new URI(u.getProtocol, u.getUserInfo, u.getHost, u.getPort, u.getPath, u.getQuery, u.getRef)
      uri.toASCIIString.replaceAll("%25", "%")
    } catch {
      case e: Exception => url
    }
  }
}
