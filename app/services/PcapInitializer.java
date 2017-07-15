package services;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import com.fasterxml.jackson.databind.JsonNode;
import org.pcap4j.core.*;
import org.pcap4j.packet.IpPacket;
import org.pcap4j.packet.Packet;
import org.pcap4j.packet.TcpPacket;
import play.Configuration;
import play.Logger;
import play.Logger.ALogger;
import play.inject.ApplicationLifecycle;
import play.libs.concurrent.HttpExecutionContext;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.io.EOFException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeoutException;

/**
 * Using Pcap4J (https://github.com/kaitoy/pcap4j#documents) to access network
 * interfaces and forward some of the packages' data (metrics) to the
 * appropriate {@link NifDispatcherActor}.
 */
@Singleton
public class PcapInitializer {

    private ALogger logger = Logger.of(PcapInitializer.class);

    private final HttpExecutionContext httpExecutionContext;
    private final ActorSystem actorSystem;
    private final ApplicationLifecycle lifecycle;
    private final PacketToJsonTransfer packetToJsonTransfer;

    /**
     * Default network interface (specified in application.conf)
     */
    private final String defaultNifName;

    /**
     * If false net-glimpse filters out its own traffic
     * (specified in application.conf)
     */
    private final boolean showOwnTraffic;

    /**
     * IP / host the Play framework is  bound to (default 0.0.0.0)
     */
    private final String httpAddress;

    /**
     * Port the Play framework is bound to (default 9000)
     */
    private final int httpPort;

    /**
     * Specifies the portion of the network packet to capture
     * https://serverfault.com/questions/253613
     */
    private final int snaplen;

    /**
     * Map: network interface name -> actor reference to {@link NifDispatcherActor}
     */
    private Map<String, ActorRef> pcapActorRefMap = new HashMap<>();

    @Inject
    public PcapInitializer(HttpExecutionContext httpExecutionContext,
            ActorSystem actorSystem,
            Configuration configuration, ApplicationLifecycle lifecycle,
            PacketToJsonTransfer packetToJsonTransfer) {
        this.httpExecutionContext = httpExecutionContext;
        this.actorSystem = actorSystem;
        this.lifecycle = lifecycle;
        this.packetToJsonTransfer = packetToJsonTransfer;

        this.defaultNifName =
                configuration.getString("nif", "empty");
        this.showOwnTraffic =
                configuration.getBoolean("showOwnTraffic", false);
        this.httpAddress = configuration.getString("play.server.http.address");
        this.httpPort = configuration.getInt("play.server.http.port");
        this.snaplen = configuration.getInt("snaplen", 65536);
    }

    public ActorRef getPcapDispatcherActorRef(String nifName) {
        if (nifName == null) {
            nifName = defaultNifName;
        }

        if (pcapActorRefMap.containsKey(nifName)) {
            return pcapActorRefMap.get(nifName);
        }
        ActorRef pcapDispatcherActorRef =
                actorSystem.actorOf(NifDispatcherActor.props(nifName));
        pcapActorRefMap.put(nifName, pcapDispatcherActorRef);

        PcapHandle pcapHandle = openPcap(nifName, snaplen);
        runPcapToDispatcher(pcapHandle, nifName);

        lifecycle.addStopHook(() -> {
            if (pcapHandle != null && pcapHandle.isOpen()) {
                pcapHandle.close();
            }
            return CompletableFuture.completedFuture(null);
        });

        return pcapDispatcherActorRef;
    }

    private PcapHandle openPcap(String networkInterfaceName, int snaplen) {
        try {
            PcapNetworkInterface nif = Pcaps.getDevByName(networkInterfaceName);
            if (nif == null) {
                throw new RuntimeException("Couldn't open network interface " +
                        networkInterfaceName);
            } else {
                logger.info(
                        "Forward network traffic from " + nif.getName() + "(" +
                                nif.getAddresses() + ")");
            }
            return nif.openLive(snaplen,
                    PcapNetworkInterface.PromiscuousMode.PROMISCUOUS, 10);
        } catch (PcapNativeException e) {
            logger.error(
                    "Couldn't open network interface " + networkInterfaceName,
                    e);
            throw new RuntimeException(e);
        }
    }

    private void runPcapToDispatcher(PcapHandle pcapHandle, String nifName) {
        Runnable r = () -> {
            pcapToDispatcher(pcapHandle, nifName);
        };
        CompletableFuture pcapToWebSocketFuture = CompletableFuture
                .runAsync(r, httpExecutionContext.current());
    }

    private void pcapToDispatcher(PcapHandle pcapHandle, String nifName) {
        // TODO Maybe there is a non-blocking way to get the packets
        while (true) {
            try {
                Packet packet = pcapHandle.getNextPacketEx();
                if (packet == null) {
                    continue;
                }
                if (!showOwnTraffic && isOurPacket(packet)) {
                    continue;
                }

                JsonNode jsonNode = packetToJsonTransfer
                        .packageToJson(packet, pcapHandle.getTimestamp());
                ActorRef pcapDispatcherActorRef =
                        pcapActorRefMap.get(nifName);
                pcapDispatcherActorRef.tell(jsonNode, ActorRef.noSender());

            } catch (NotOpenException e) {
                break;
            } catch (PcapNativeException | EOFException | TimeoutException e) {
                continue;
            }
        }
    }

    /**
     * Check if this packet is one of our WebSocket packets: our WebSocket
     * packets are TCP packets that originate from our IP and port - or are
     * destined to our IP and port
     */
    private boolean isOurPacket(Packet packet) {
        if (!packet.contains(IpPacket.class) ||
                !packet.contains(TcpPacket.class)) {
            return false;
        }

        IpPacket.IpHeader ipHeader = packet.get(IpPacket.class).getHeader();
        String srcAddr = ipHeader.getSrcAddr().getHostAddress();
        String dstAddr = ipHeader.getDstAddr().getHostAddress();
        TcpPacket.TcpHeader tpcHeader = packet.get(TcpPacket.class).getHeader();
        int tcpSrcPort = tpcHeader.getSrcPort().valueAsInt();
        int tcpDstPort = tpcHeader.getDstPort().valueAsInt();

        // Check if our IP and port are equal to the packet's source IP and port
        // If our IP is 0.0.0.0 we listen on every IP
        if ((httpAddress.equals("0.0.0.0") || httpAddress.equals(srcAddr)) &&
                httpPort == tcpSrcPort) {
            return true;
        }

        // Check if our IP and port are equal to the packet's destination IP and port
        // If our IP is 0.0.0.0 we listen on every IP
        if ((httpAddress.equals("0.0.0.0") || httpAddress.equals(dstAddr)) &&
                httpPort == tcpDstPort) {
            return true;
        }

        return false;
    }
}
