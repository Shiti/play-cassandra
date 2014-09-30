import sbt._
import Keys._
import play.Play.autoImport._
import PlayKeys._

object AppBuild extends Build {

  val appName = "CantabileDemo"
  val appVersion = "1.0"

  val scalaVersion = "2.10.4"

  val appDependencies = Seq(
    "com.tuplejump" %% "cantabile" % "1.0.0-SNAPSHOT"
  )

  val main = Project(appName, file(".")).enablePlugins(play.PlayScala).settings(
    version := appVersion,
    libraryDependencies ++= appDependencies
  )
}
