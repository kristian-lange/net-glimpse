import com.google.inject.AbstractModule
import org.pcap4j.core.{PcapNativeException, Pcaps}
import play.api.Logger

class Module extends AbstractModule {

  private val logger: Logger = Logger(this.getClass)

  def configure() = {
    // Print out all network interfaces so users know which names they have
    try {
      import scala.collection.JavaConversions._
      for (nif <- Pcaps.findAllDevs) {
        Logger.info("NIF: " + nif.toString)
      }
    } catch {
      case e: PcapNativeException =>
      // do nothing
    }
  }
}
