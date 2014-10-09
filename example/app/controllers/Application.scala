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
