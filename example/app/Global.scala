import com.tuplejump.cantabile.CasPlugin
import play.api.{Application, GlobalSettings}

object Global extends GlobalSettings{
  override def onStart(app: Application): Unit = {
    CasPlugin.load("evolutions/cassandra/1.cql")
  }

}