import sbt._
import BuildConfig.Dependencies

lazy val commonSettings = BuildConfig.commonSettings()

lazy val global = project.settings(commonSettings).settings(
  Seq(
    name := "paradox-scala-global",

    libraryDependencies ++= Seq() ++ Dependencies.testDeps
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
).aggregate()
// custom alias to hook in any other custom commands
addCommandAlias("build", "; compile")
