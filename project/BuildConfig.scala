import sbt.Keys._
import sbt._

object BuildConfig {
  object Dependencies {
    val testDeps = Seq(
      "org.scalatest" %% "scalatest" % versions.scalatest,
      "org.mockito" % "mockito-all" % versions.mockito,
      "com.github.alexarchambault" %% "scalacheck-shapeless_1.13" % "1.1.5"
    ).map(_ % "test")
  }

  object Revision {
    lazy val version = System.getProperty("version", "1.0-SNAPSHOT")
  }

  object versions {
    val mockito = "1.10.19"
    val scalatest = "3.0.1"
  }

  def commonSettings() = {
    Seq(
      organization := "io.paradoxical",

      version := BuildConfig.Revision.version,

      resolvers += Resolver.sonatypeRepo("releases"),

      scalaVersion := "2.12.4",

      crossScalaVersions := Seq("2.11.8", scalaVersion.value),

      scalacOptions ++= Seq(
        "-deprecation",
        "-encoding", "UTF-8",
        "-feature",
        "-language:existentials",
        "-language:higherKinds",
        "-language:implicitConversions",
        "-language:postfixOps",
        "-language:experimental.macros",
        "-unchecked",
        "-Ywarn-nullary-unit",
        "-Xfatal-warnings",
        "-Xfuture"
      ) ++ (CrossVersion.partialVersion(scalaVersion.value) match {
        case Some((2, 12)) => Seq("-Xlint:-unused")
        case _ => Seq("-Xlint")
      }),

      scalacOptions in doc := scalacOptions.value.filterNot(_ == "-Xfatal-warnings"),

      publishMavenStyle := true,

      publishTo := Some(
        if (isSnapshot.value)
          Opts.resolver.sonatypeSnapshots
        else
          Opts.resolver.sonatypeStaging
      )
    ) ++ Publishing.publishSettings
  }
}
