package com.tuplejump.cantabile

import com.datastax.driver.core.exceptions.NoHostAvailableException
import com.datastax.driver.core.{Cluster, Session}
import play.api.Play.current
import play.api.{Application, Play, Plugin}

import scala.io.Source

class CasPlugin(app: Application) extends Plugin {

  private var _helper: Option[CassandraHelper] = None

  def helper = _helper.getOrElse(throw new RuntimeException("CasPlugin error: no CassandraHelper available?"))

  override def onStart() = {

    val appConfig = app.configuration.underlying.getConfig("cantabile")

    val hosts: Array[java.lang.String] = appConfig.getString("host").split(",").map(_.trim)

    val port: Int = appConfig.getInt("port")

    val cluster = Cluster.builder()
      .addContactPoints(hosts: _*)
      .withPort(port).build()

    _helper = try {
      val session = cluster.connect()
      Some(CassandraHelper(hosts, port, cluster, session))
    } catch {
      case e: NoHostAvailableException =>
        val msg =
          s"""Failed to initialize CasPlugin.
             |Please check if Cassandra is accessible at
             | ${hosts.head}:$port or update configuration""".stripMargin
        throw app.configuration.globalError(msg)
    }
  }

  override def onStop() = {
    helper.session.close()
    helper.cluster.close()
  }

  override def enabled = true

}

object CasPlugin {
  private val casPlugin = Play.application.plugin[CasPlugin].get

  private val cassandraHelper = casPlugin.helper

  def hosts: Array[java.lang.String] = cassandraHelper.hosts

  def port: Int = cassandraHelper.port

  def cluster: Cluster = cassandraHelper.cluster

  def session: Session = cassandraHelper.session

  def load(fileName: String): Unit = {

    val lines = Source.fromURL(getClass.getClassLoader.getResource(fileName)).getLines().toList

    val stmnts = lines.filterNot {
      l => l.length == 0 || isComment(l)
    }.mkString("").split(";")

      stmnts.map {
        line =>
            cassandraHelper.session.execute(line)
      }
  }

  private def isComment(statement: String): Boolean = {
    statement.startsWith("#")
  }
}

private[cantabile] case class CassandraHelper(hosts: Array[java.lang.String], port: Int, cluster: Cluster, session: Session)