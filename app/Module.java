import com.google.inject.AbstractModule;
import org.pcap4j.core.PcapNativeException;
import org.pcap4j.core.PcapNetworkInterface;
import org.pcap4j.core.Pcaps;
import play.Logger;
import play.libs.akka.AkkaGuiceSupport;

/**
 * This class is a Guice module that tells Guice how to bind several
 * different types. This Guice module is created when the Play
 * application starts.
 * <p>
 * Play will automatically use any class called `Module` that is in
 * the root package. You can create modules in other locations by
 * adding `play.modules.enabled` settings to the `application.conf`
 * configuration file.
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
