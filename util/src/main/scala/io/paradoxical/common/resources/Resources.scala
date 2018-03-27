package io.paradoxical.common.resources

import java.io.File
import java.net.URL
import scala.collection.JavaConverters._
import scala.io.{BufferedSource, Source}

object Resources {
  def load(path: String): BufferedSource = {
    Source.fromURL(getClass.getClassLoader.getResource(path.stripPrefix("/")))
  }

  def resourceFiles(directory: String): List[URL] = {
    getClass.getClassLoader.getResources(directory).
      asScala.
      toList.
      map(x => new File(x.getFile)).
      flatMap(_.listFiles()).
      map(_.toURI.toURL)
  }
}
