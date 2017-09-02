import com.google.inject.AbstractModule
import org.pcap4j.core.{PcapNativeException, Pcaps}
import play.api.Logger

import scala.collection.JavaConverters._

class Module extends AbstractModule {

  private val logger: Logger = Logger(this.getClass)

  def configure() = {
    // Print out all network interfaces so users know which names they have
    try {
      for (nif <- Pcaps.findAllDevs.asScala) {
        Logger.info("NIF: " + nif.toString)
      }
    } catch {
      case e: PcapNativeException =>
      // do nothing
    }
  }
}
