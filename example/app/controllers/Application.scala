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

package controllers

import java.util.UUID

import play.api.libs.json.Json
import play.api.mvc._
import models._

object Application extends Controller {

  val playlistId = UUID.fromString("62c36092-82a1-3a00-93d1-46196ee77204")

  def addSampleSongs = Action {
    Playlist.addSongs(Playlist.sampleSongs, playlistId)
    val result = Playlist.getSongTitles(playlistId)
    Ok(views.html.index(result))
  }

  def getTitles = Action {
    val result = Playlist.getSongTitles(playlistId)
    Ok(views.html.index(result))
  }
}
