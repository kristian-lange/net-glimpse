import com.google.inject.AbstractModule
import org.pcap4j.core.{PcapNativeException, Pcaps}
import play.api.{Configuration, Environment, Logger}

import scala.collection.JavaConverters._

class Module(environment: Environment, configuration: Configuration) extends AbstractModule {

  private val logger: Logger = Logger(this.getClass)

  def configure() = {
    // Print out all network interfaces so users know which names they have
    try {
      val nifs = Pcaps.findAllDevs.asScala

      nifs.foreach(nif => Logger.info("NIF: " + nif.toString))

      System.out.println("List of available network interfaces:")
      nifs.foreach(nif => System.out.println(s"  ${nif.getName}"))

      var httpAddress = configuration.get[String]("play.server.http.address")
      if (httpAddress == "0.0.0.0") httpAddress = "localhost"
      val httpPort = configuration.get[Int]("play.server.http.port")
      val firstNif = if (nifs.nonEmpty) nifs.head.getName else "myNif"
      System.out.println(s"E.g. in your browser use URL " +
          s"$httpAddress:$httpPort/glimpse?nif=$firstNif")
    } catch {
      case e: PcapNativeException =>
      // do nothing
    }
  }
}
