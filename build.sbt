import sbt.{CrossVersion, _}
import BuildConfig.Dependencies

lazy val commonSettings = BuildConfig.commonSettings()

lazy val global = project.settings(commonSettings).settings(
  Seq(
    name := "paradox-scala-global",
    libraryDependencies ++= Seq() ++ Dependencies.testDeps
  )
)

lazy val util = project.settings(commonSettings).settings(
  Seq(
    name := "paradox-scala-util",
    libraryDependencies ++= Seq(
      "com.google.guava" % "guava" % "21.0",
      "joda-time" % "joda-time" % "2.9.7",
      "org.joda" % "joda-convert" % "1.8.1",
      "com.google.code.findbugs" % "jsr305" % "3.0.2",
      "org.slf4j" % "slf4j-api" % "1.7.25",
      "org.typelevel" %% "macro-compat" % "1.1.1",
      "org.scala-lang" % "scala-compiler" % scalaVersion.value % "provided",
      compilerPlugin("org.scalamacros" % "paradise" % "2.1.0" cross CrossVersion.patch)
    ) ++ Dependencies.testDeps
  )
).dependsOn(global)

lazy val `config-api` = project.settings(commonSettings).settings(
  Seq(
    name := "paradox-scala-config-api"
  )
)

lazy val jackson = project.settings(commonSettings).settings(
  Seq(
    name := "paradox-scala-jackson",

    libraryDependencies ++= Seq(
      "com.fasterxml.jackson.module" %% "jackson-module-scala" % "2.8.10"
    ) ++ Dependencies.testDeps
  )
).dependsOn(global)

lazy val `scala-global` = project.in(file(".")).settings(commonSettings).settings(
  aggregate in update := false,
  publishArtifact := false
).aggregate(jackson, global, util, `config-api`)

// custom alias to hook in any other custom commands
addCommandAlias("build", "; compile")
