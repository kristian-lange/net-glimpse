package services;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.pcap4j.core.*;
import org.pcap4j.packet.*;
import play.Configuration;
import play.Logger;
import play.Logger.ALogger;
import play.inject.ApplicationLifecycle;
import play.libs.Json;
import play.libs.concurrent.HttpExecutionContext;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * Using Pcap4J (https://github.com/kaitoy/pcap4j#documents) to access network
 * interfaces and forward some of the packages' data (metrics) to the appropriate {@link NifDispatcherActor}.
 *
 * TODO refactor to many concerns in this class
 */
@Singleton
public class PcapInitializer {

    private ALogger logger = Logger.of(PcapInitializer.class);

    private final HttpExecutionContext httpExecutionContext;
    private final ActorSystem actorSystem;
    private final ApplicationLifecycle lifecycle;

    /**
     * Default network interface
     */
    private final String defaultNifName;

    /**
     *  Specifies the portion of the network packet to capture
     *  https://serverfault.com/questions/253613
     */
    private final int snaplen;

    /**
     * Map: network interface name -> actor reference to {@link NifDispatcherActor}
     */
    private Map<String, ActorRef> pcapActorRefMap = new HashMap<>();

    @Inject
    public PcapInitializer(HttpExecutionContext httpExecutionContext, ActorSystem actorSystem,
            Configuration configuration, ApplicationLifecycle lifecycle) {
        this.httpExecutionContext = httpExecutionContext;
        this.actorSystem = actorSystem;
        this.lifecycle = lifecycle;

        this.defaultNifName =
                configuration.getString("nif", "empty");
        this.snaplen = configuration.getInt("snaplen", 65536);
    }

    public ActorRef getPcapDispatcherActorRef(String nifName) {
        if (nifName == null) {
            nifName = defaultNifName;
        }

        if(pcapActorRefMap.containsKey(nifName)) {
            return pcapActorRefMap.get(nifName);
        }
        ActorRef pcapDispatcherActorRef = actorSystem.actorOf(NifDispatcherActor.props(nifName));
        pcapActorRefMap.put(nifName, pcapDispatcherActorRef);

        PcapHandle pcapHandle = openPcap(nifName, snaplen);
        runPcapToDispatcher(pcapHandle, nifName);

        lifecycle.addStopHook(() -> {
            if(pcapHandle != null && pcapHandle.isOpen()) {
                pcapHandle.close();
            }
            return CompletableFuture.completedFuture(null);
        });

        return pcapDispatcherActorRef;
    }

    private PcapHandle openPcap(String networkInterfaceName, int snaplen) {
        try {
            PcapNetworkInterface nif = Pcaps.getDevByName(networkInterfaceName);
            if(nif == null) {
                throw new RuntimeException("Couldn't open network interface " +
                        networkInterfaceName);
            } else {
                logger.info(
                        "Forward network traffic from " + nif.getName() + "(" +
                                nif.getAddresses() + ")");
            }
            return nif.openLive(snaplen,
                    PcapNetworkInterface.PromiscuousMode.PROMISCUOUS, 10);
        } catch(PcapNativeException e) {
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
        while(true) {
            try {
                // TODO Maybe there is a non-blocking way to get the packets
                Packet packet = pcapHandle.getNextPacket();
                if(packet != null) {
                    writePacketMetricsToDispatcher(packet, pcapHandle, nifName);
                }
            } catch(NotOpenException e) {
                break;
            }
        }
    }

    private void writePacketMetricsToDispatcher(Packet packet,
            PcapHandle pcapHandle, String nifName) {
        ObjectNode outNode = Json.newObject();
        outNode.put("timestamp", pcapHandle.getTimestamp().toString());
        fillNodeWithEthernetPacketMetrics(outNode,
                packet.get(EthernetPacket.class));
        fillNodeWithIpPacketMetrics(outNode, packet.get(IpPacket.class));
        fillNodeWithArpPacketMetrics(outNode, packet.get(ArpPacket.class));
        fillNodeWithTcpPacketMetrics(outNode, packet.get(TcpPacket.class));
        fillNodeWithUdpPacketMetrics(outNode, packet.get(UdpPacket.class));

        ActorRef pcapDispatcherActorRef = pcapActorRefMap.get(nifName);
        pcapDispatcherActorRef.tell(outNode, ActorRef.noSender());
    }

    private void fillNodeWithEthernetPacketMetrics(ObjectNode outNode,
            EthernetPacket ethernetPacket) {
        if(ethernetPacket != null) {
            EthernetPacket.EthernetHeader header =
                    ethernetPacket.getHeader();
            outNode.put("macSrcAddr", header.getSrcAddr().toString());
            outNode.put("macDstAddr", header.getDstAddr().toString());
            outNode.put("macType", header.getType().name());
        }
    }

    private void fillNodeWithIpPacketMetrics(ObjectNode outNode,
            IpPacket ipPacket) {
        if(ipPacket != null) {
            IpPacket.IpHeader header = ipPacket.getHeader();
            outNode.put("ipSrcAddr", header.getSrcAddr().toString());
            outNode.put("ipDstAddr", header.getDstAddr().toString());
            outNode.put("ipProtocol",
                    header.getProtocol().name());
            outNode.put("ipVersion",
                    header.getVersion().name());
        }
    }

    private void fillNodeWithArpPacketMetrics(ObjectNode outNode,
            ArpPacket arpPacket) {
        if(arpPacket != null) {
            ArpPacket.ArpHeader header = arpPacket.getHeader();
            outNode.put("arpSrcHardwareAddr",
                    header.getSrcHardwareAddr().toString());
            outNode.put("arpSrcProtocolAddr",
                    header.getSrcProtocolAddr().toString());
            outNode.put("arpDstHardwareAddr",
                    header.getDstHardwareAddr().toString());
            outNode.put("arpDstProtocolAddr",
                    header.getDstProtocolAddr().toString());
            outNode.put("arpHardwareType",
                    header.getHardwareType().name());
            outNode.put("arpOperation",
                    header.getOperation().name());
            outNode.put("arpProtocolType",
                    header.getProtocolType().name());
        }
    }

    private void fillNodeWithTcpPacketMetrics(ObjectNode outNode,
            TcpPacket tcpPacket) {
        if(tcpPacket != null) {
            outNode.put("tcpSrcPort", "" + tcpPacket.getHeader().getSrcPort());
            outNode.put("tcpDstPort", "" + tcpPacket.getHeader().getDstPort());
        }
    }

    private void fillNodeWithUdpPacketMetrics(ObjectNode outNode,
            UdpPacket udpPacket) {
        if(udpPacket != null) {
            outNode.put("udpSrcPort", "" + udpPacket.getHeader().getSrcPort());
            outNode.put("udpDstPort", "" + udpPacket.getHeader().getDstPort());
        }
    }

}
