/*
 * Licensed to Tuplejump Software Pvt. Ltd. under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. Tuplejump Software Pvt. Ltd. licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import sbt._
import sbt.Keys._

object PluginBuild extends Build {

  lazy val cassandraPlugin = Project(
    id = "play-cassandra",
    base = file("."),
    settings = Seq(
      name := "play-cassandra",
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
        "com.typesafe.play" %% "play" % "2.3.0" % "provided" cross CrossVersion.binary
      )
    )
  )
}
