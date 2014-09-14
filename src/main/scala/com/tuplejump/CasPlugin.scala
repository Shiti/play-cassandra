package com.tuplejump

import com.datastax.driver.core.exceptions.NoHostAvailableException
import com.datastax.driver.core.{SimpleStatement, BatchStatement, Cluster, Session}
import com.typesafe.config.ConfigFactory
import play.api.{Play, Application, Logger, Plugin}
import play.api.Play.current

import scala.io.Source

class CasPlugin(app: Application) extends Plugin {
  private var hosts: Array[java.lang.String] = Array("localhost")
  private var port: Int = 9042
  private var cluster: Cluster = null
  private var session: Session = null

  override def onStart() = {

    val appConfig = ConfigFactory.load().getConfig("cantabile")

    hosts = appConfig.getString("host").split(",").map(_.trim)

    port = appConfig.getInt("port")

    cluster = Cluster.builder()
      .addContactPoints(hosts: _*)
      .withPort(port).build()

    try {
      session = cluster.connect()
      Logger.debug("started session")
    } catch {
      case e: NoHostAvailableException =>
        Logger.error(s"Failed to initialize CasPlugin. Please check if Cassandra is accessible at ${hosts.head}:$port or update configuration")
        throw new RuntimeException(s"Failed to initialize CasPlugin. Please check if Cassandra is accessible at ${hosts.head}:$port or update configuration", e)
    }
  }

  override def onStop() = {
    session.close()
    cluster.close()
  }

  override def enabled = true

  def getHost: Array[java.lang.String] = hosts

  def getPort: Int = port

  def getSession: Session = session

  def getCluster: Cluster = cluster

}

object Cantabile {
  private val casPlugin = Play.application.plugin[CasPlugin].get

  val host = casPlugin.getHost
  val port = casPlugin.getPort
  val cluster = casPlugin.getCluster
  val session = casPlugin.getSession

  def load(fileName:String) = {

    val batchStmt = new BatchStatement()
    val initSource = Source.fromURL(getClass.getClassLoader.getResource(fileName)).getLines().toList

    initSource.map {
      line =>
        if (isValidCQLStatement(line)) {
          batchStmt.add(new SimpleStatement(line))
        }
    }

    try {
      session.execute(batchStmt)
    } catch {
      case ex: Throwable =>
        Logger.error(s"Failed to load script $fileName")
        throw new RuntimeException(s"Failed to load script $fileName",ex)
    }


  }

  private def isValidCQLStatement(statement: String): Boolean = {
    statement.length > 2 && !statement.startsWith("#") && statement.endsWith(";")
  }
}

