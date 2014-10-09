package com.tuplejump.plugin

import java.util.Date

import com.datastax.driver.core.exceptions.NoHostAvailableException
import com.datastax.driver.core.{Cluster, Session}
import play.api.Play.current
import play.api.{Application, Play, Plugin}

import scala.io.Source
import scala.util.{Failure, Success, Try}

class CasPlugin(app: Application) extends Plugin {

  private var _helper: Option[CassandraConnection] = None

  def helper = _helper.getOrElse(throw new RuntimeException("CasPlugin error: no CassandraHelper available?"))

  override def onStart() = {

    val appConfig = app.configuration.getConfig("casplugin").get
    val appName: String = appConfig.getString("appName").getOrElse("appWithCasPlugin")

    val isEvolutionEnabled: Boolean = appConfig.getBoolean("evolution.enabled").getOrElse(true)
    val scriptSource: String = appConfig.getString("evolution.directory").getOrElse("evolutions/cassandra/")

    val hosts: Array[java.lang.String] = appConfig.getString("host").getOrElse("localhost").split(",").map(_.trim)
    val port: Int = appConfig.getInt("port").getOrElse(9042)

    val cluster = Cluster.builder()
      .addContactPoints(hosts: _*)
      .withPort(port).build()

    _helper = try {
      val session = cluster.connect()
      Util.loadScript("casPlugin.cql", session)
      if (isEvolutionEnabled) {
        CasHelper.applyEvolution(session, appName, scriptSource)
      }
      Some(CassandraConnection(hosts, port, cluster, session))
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

private[plugin] object Util {
  private def isComment(statement: String): Boolean = {
    statement.startsWith("#")
  }

  private def isValidStatement(str: String): Boolean = {
    val line = str.trim
    line.length == 0 || isComment(line)
  }

  def getValidStatements(lines: Iterator[String]): Array[String] = {
    lines.filterNot(isValidStatement).mkString("").split(";")
  }

  private def getValidStatementsFromFile(fileName: String): Array[String] = {
    val lines = Source.fromURL(getClass.getClassLoader.getResource(fileName)).getLines()
    getValidStatements(lines)
  }

  def executeStmnts(stmnts: Array[String], session: Session) = {
    stmnts.foreach {
      line =>
        try {
          session.execute(line)
        } catch {
          case ex: Throwable =>
            throw new RuntimeException(s"Failed to execute $line", ex)
        }
    }
  }

  def loadScript(fileName: String, session: Session): Unit = {
    val stmnts = getValidStatementsFromFile(fileName)
    executeStmnts(stmnts, session)
  }

}

private[plugin] object CasHelper {

  import com.datastax.driver.core.querybuilder.{QueryBuilder => QB}

  import scala.collection.JavaConversions._

  case class LastUpdate(revision: Int, appliedAt: Date)

  private val Keyspace = "casplugin"
  private val Table = "revision_history"
  private val AppIDColumn = "app_id"
  private val RevisionColumn = "revision"
  private val TimeColumn = "applied_at"

  private def getLastUpdate(session: Session, appName: String): LastUpdate = {
    val query = QB.select(RevisionColumn, TimeColumn)
      .from(Keyspace, Table)
      .where(QB.eq(AppIDColumn, appName))

    val row = session.execute(query).toIterable.headOption

    val result = row match {
      case Some(r) =>
        LastUpdate(r.getInt(RevisionColumn), r.getDate(TimeColumn))
      case None =>
        LastUpdate(0, new Date())
    }
    result
  }

  private def updateRevision(session: Session, appName: String, revision: Int) = {
    val query = QB.update(Keyspace, Table)
      .`with`(QB.set(RevisionColumn, revision))
      .and(QB.set(TimeColumn, new Date()))
      .where(QB.eq(AppIDColumn, appName))

    session.execute(query)
  }

  def applyEvolution(session: Session, appName: String, dirName: String) = {
    val lastUpdate = getLastUpdate(session, appName)
    val currentRevision = lastUpdate.revision + 1
    val fileName: String = s"$dirName$currentRevision.cql"

    val mayBeLines = Try(Source.fromURL(getClass.getClassLoader.getResource(fileName)).getLines())

    mayBeLines match {
      case Success(lines) =>
        val stmt = Util.getValidStatements(lines)
        Util.executeStmnts(stmt, session)
        updateRevision(session, appName, currentRevision)
      case Failure(e: NullPointerException) =>
      case Failure(e) => throw e
    }

  }
}

object CasPlugin {
  private val casPlugin = Play.application.plugin[CasPlugin].get

  private val cassandraHelper = casPlugin.helper

  def hosts: Array[java.lang.String] = cassandraHelper.hosts

  def port: Int = cassandraHelper.port

  def cluster: Cluster = cassandraHelper.cluster

  def session: Session = cassandraHelper.session

  def loadCQLFile(fileName: String): Unit = {
    Util.loadScript(fileName, cassandraHelper.session)
  }

}

private[plugin] case class CassandraConnection(hosts: Array[java.lang.String], port: Int, cluster: Cluster, session: Session)