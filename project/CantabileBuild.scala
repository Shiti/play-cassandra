import sbt._
import sbt.Keys._

object CantabileBuild extends Build {

  lazy val cantabile = Project(
    id = "cantabile",
    base = file("."),
    settings = Seq(
      name := "cantabile",
      organization := "com.tuplejump",
      version := "1.0.0-SNAPSHOT",
      scalaVersion := "2.10.4",
      crossScalaVersions := Seq("2.10.4"),
      crossVersion := CrossVersion.binary,
      resolvers := Seq(
        "Sonatype snapshots" at "http://oss.sonatype.org/content/repositories/snapshots/",
        "Sonatype" at "http://oss.sonatype.org/content/groups/public/",
        "Typesafe repository releases" at "http://repo.typesafe.com/typesafe/releases/",
        "Typesafe repository snapshots" at "http://repo.typesafe.com/typesafe/snapshots/"
      ),
      libraryDependencies ++= Seq(
        "com.datastax.cassandra" % "cassandra-driver-core" % "2.0.4",
        "com.typesafe.play" %% "play" % "2.3.0" % "provided" cross CrossVersion.binary,
        "com.typesafe.play" %% "play-test" % "2.3.0" % "test" cross CrossVersion.binary
      )
    )
  )
}
