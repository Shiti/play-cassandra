package models

import java.util.UUID

import com.datastax.driver.core.BatchStatement
import com.datastax.driver.core.querybuilder.QueryBuilder
import com.tuplejump.plugin.CasPlugin
import scala.collection.JavaConversions._

case class Song(id: UUID, title: String, artist: String, album: String)

object Playlist {

  val sampleSongs: Seq[Song] = Seq(
    Song(UUID.fromString("a3e64f8f-bd44-4f28-b8d9-6938726e34d4"), "La Grange", "ZZ Top", "Tres Hombres"),
    Song(UUID.fromString("8a172618-b121-4136-bb10-f665cfc469eb"), "Moving in Stereo", "Fu Manchu", "We Must Obey"),
    Song(UUID.fromString("2b09185b-fb5a-4734-9b56-49077de9edbf"), "Outside Woman Blues", "Back Door Slam", "Roll Away")
  )

  def addSongs(songs: Seq[Song], playlistId: UUID) = {

    val insertQuery: String = "INSERT INTO music.playlists (id, song_id, title, artist, album) VALUES (?, ?, ?, ?, ?)"

    val ps = CasPlugin.session.prepare(insertQuery)
    var batch = new BatchStatement()

    songs.foreach {
      s =>
        batch.add(ps.bind(playlistId, s.id, s.title, s.artist, s.album))
    }
    CasPlugin.session.execute(batch)

  }

  def getSongTitles(playlistId: UUID): Seq[String] = {
    val query = QueryBuilder.select("title")
      .from("music", "playlists")
      .where(QueryBuilder.eq("id", playlistId))
    val queryResult = CasPlugin.session.execute(query).toIterable
    val availableTitles = queryResult.map {
      row => row.getString("title")
    }.toSeq
    availableTitles
  }
}