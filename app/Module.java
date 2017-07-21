import com.google.inject.AbstractModule;
import org.pcap4j.core.PcapNativeException;
import org.pcap4j.core.PcapNetworkInterface;
import org.pcap4j.core.Pcaps;
import play.Logger;
import play.libs.akka.AkkaGuiceSupport;

/**
 * Created by Kristian Lange on 2017.
 */
public class Module extends AbstractModule implements AkkaGuiceSupport {

    private Logger.ALogger logger = Logger.of(Module.class);

    @Override
    protected void configure() {
        // Print out all network interfaces so users know which names they have
        try {
            for (PcapNetworkInterface nif : Pcaps.findAllDevs()) {
                Logger.info("NIF: " + nif.toString());
            }
        } catch (PcapNativeException e) {
            // do nothing
        }
    }
}
