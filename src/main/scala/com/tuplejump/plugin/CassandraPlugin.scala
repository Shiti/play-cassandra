package com.tuplejump.plugin

import java.util.Date

import com.datastax.driver.core.exceptions.NoHostAvailableException
import com.datastax.driver.core.{Cluster, Session}
import play.api.Play.current
import play.api.{Application, Play, Plugin}

import scala.io.Source
import scala.util.{Failure, Success, Try}

class CassandraPlugin(app: Application) extends Plugin {

  private var _helper: Option[CassandraConnection] = None

  def helper = _helper.getOrElse(throw new RuntimeException("CassandraPlugin error: CassandraHelper initialization failed"))

  override def onStart() = {

    val appConfig = app.configuration.getConfig("cassandraPlugin").get
    val appName: String = appConfig.getString("appName").getOrElse("appWithCassandraPlugin")

    val isEvolutionEnabled: Boolean = appConfig.getBoolean("evolution.enabled").getOrElse(true)
    val scriptSource: String = appConfig.getString("evolution.directory").getOrElse("evolutions/cassandra/")

    val hosts: Array[java.lang.String] = appConfig.getString("host").getOrElse("localhost").split(",").map(_.trim)
    val port: Int = appConfig.getInt("port").getOrElse(9042)

    val cluster = Cluster.builder()
      .addContactPoints(hosts: _*)
      .withPort(port).build()

    _helper = try {
      val session = cluster.connect()
      Util.loadScript("cassandraPlugin.cql", session)
      if (isEvolutionEnabled) {
        Evolutions.applyEvolution(session, appName, scriptSource)
      }
      Some(CassandraConnection(hosts, port, cluster, session))
    } catch {
      case e: NoHostAvailableException =>
        val msg =
          s"""Failed to initialize CassandraPlugin.
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

private[plugin] object Evolutions {

  import com.datastax.driver.core.querybuilder.{QueryBuilder => QB}

  import scala.collection.JavaConversions._

  case class LastUpdate(revision: Int, appliedAt: Date)

  private val Keyspace = "cassandra_play_plugin"
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

object Cassandra {
  private val casPlugin = Play.application.plugin[CassandraPlugin].get

  private val cassandraHelper = casPlugin.helper

  /**
   * gets the Cassandra hosts provided in the configuration
   */
  def hosts: Array[java.lang.String] = cassandraHelper.hosts

  /**
   * gets the port number on which Cassandra is running from the configuration
   */
  def port: Int = cassandraHelper.port

  /**
   * gets a reference of the started Cassandra cluster
   * The cluster is built with the configured set of initial contact points
   * and policies at startup
   */
  def cluster: Cluster = cassandraHelper.cluster

  /**
   * gets a reference of the started Cassandra session
   * A new session is created on the cluster at startup
   */
  def session: Session = cassandraHelper.session

  /**
   * executes CQL statements available in given file.
   * Empty lines or lines starting with `#` are ignored.
   * Each statement can extend over multiple lines and must end with a semi-colon.
   * @param fileName - name of the file
   */
  def loadCQLFile(fileName: String): Unit = {
    Util.loadScript(fileName, cassandraHelper.session)
  }

}

private[plugin] case class CassandraConnection(hosts: Array[java.lang.String],
                                               port: Int,
                                               cluster: Cluster,
                                               session: Session)