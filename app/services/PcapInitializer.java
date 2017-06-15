package services;

import org.pcap4j.core.*;
import play.Configuration;
import play.Logger;
import play.Logger.ALogger;
import play.inject.ApplicationLifecycle;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.concurrent.CompletableFuture;

/**
 * Using Pcap4J (https://github.com/kaitoy/pcap4j#documents) to access network interfaces
 */
@Singleton
public class PcapInitializer {

	private ALogger logger = Logger.of(PcapInitializer.class);

	private final String networkInterfaceName;
	private final PcapHandle pcapHandle;

	@Inject
	public PcapInitializer(ApplicationLifecycle lifecycle,
			Configuration configuration) {
		this.networkInterfaceName =
				configuration.getString("networkinterface", "empty");
		int snaplen = configuration.getInt("snaplen", 65536);

		pcapHandle = openPcap(networkInterfaceName, snaplen);

		lifecycle.addStopHook(() -> {
			pcapHandle.close();
			return CompletableFuture.completedFuture(null);
		});
	}

	private PcapHandle openPcap(String networkInterfaceName, int snaplen) {
		try {
			PcapNetworkInterface nif = Pcaps.getDevByName(networkInterfaceName);
			if (nif == null) {
				throw new RuntimeException("Couldn't open network interface " + networkInterfaceName);
			} else {
				logger.info("Forward network traffic from " + nif.getName() + "(" +
						nif.getAddresses() + ")");
			}
			return nif.openLive(snaplen,
					PcapNetworkInterface.PromiscuousMode.PROMISCUOUS, 10);
		} catch (PcapNativeException e) {
			logger.error("Couldn't open network interface " + networkInterfaceName, e);
			throw new RuntimeException(e);
		}
	}

	public String getNetworkInterfaceName() {
		return networkInterfaceName;
	}

	public PcapHandle getPcapHandle() {
		return pcapHandle;
	}
}
