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
import Keys._
import play.Play.autoImport._
import PlayKeys._

object AppBuild extends Build {

  val appName = "PlayCassandraDemo"
  val appVersion = "1.0"

  val scalaVersion = "2.10.4"

  val appDependencies = Seq(
    "com.tuplejump" %% "play-cassandra" % "1.0.0-SNAPSHOT"
  )

  val main = Project(appName, file(".")).enablePlugins(play.PlayScala).settings(
    version := appVersion,
    libraryDependencies ++= appDependencies
  )
}
