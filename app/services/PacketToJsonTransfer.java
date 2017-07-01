package services;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.pcap4j.packet.*;
import play.libs.Json;

import javax.inject.Singleton;
import java.sql.Timestamp;

@Singleton
public class PacketToJsonTransfer {

    public JsonNode packageToJson(Packet packet, Timestamp timestamp) {
        ObjectNode jsonNode = Json.newObject();
        jsonNode.put("timestamp", timestamp.toString());

        if (packet.contains(EthernetPacket.class)) {
            jsonNode.set("ethernet",
                    getEthernetPacketMetrics(packet.get(EthernetPacket.class)));
        }
        if (packet.contains(ArpPacket.class)) {
            jsonNode.set("arp",
                    getArpPacketMetrics(packet.get(ArpPacket.class)));
        }
        if (packet.contains(IpPacket.class)) {
            jsonNode.set("ip",
                    getIpPacketMetrics(packet.get(IpPacket.class)));
        }
        if (packet.contains(TcpPacket.class)) {
            jsonNode.set("tcp",
                    getTcpPacketMetrics(packet.get(TcpPacket.class)));
        }
        if (packet.contains(UdpPacket.class)) {
            jsonNode.set("udp",
                    getUdpPacketMetrics(packet.get(UdpPacket.class)));
        }
        return jsonNode;
    }

    private JsonNode getEthernetPacketMetrics(
            EthernetPacket ethernetPacket) {
        ObjectNode jsonNode = Json.newObject();
        EthernetPacket.EthernetHeader header = ethernetPacket.getHeader();
        jsonNode.put("macSrcAddr", header.getSrcAddr().toString());
        jsonNode.put("macDstAddr", header.getDstAddr().toString());
        jsonNode.put("etherType", header.getType().name());
        return jsonNode;
    }

    private JsonNode getArpPacketMetrics(ArpPacket arpPacket) {
        ObjectNode jsonNode = Json.newObject();
        ArpPacket.ArpHeader header = arpPacket.getHeader();
        jsonNode.put("srcHardwareAddr", header.getSrcHardwareAddr().toString());
        jsonNode.put("srcProtocolAddr", header.getSrcProtocolAddr().toString());
        jsonNode.put("dstHardwareAddr", header.getDstHardwareAddr().toString());
        jsonNode.put("dstProtocolAddr", header.getDstProtocolAddr().toString());
        jsonNode.put("hardwareType", header.getHardwareType().name());
        jsonNode.put("operation", header.getOperation().name());
        jsonNode.put("protocolType", header.getProtocolType().name());
        return jsonNode;
    }

    private JsonNode getIpPacketMetrics(IpPacket ipPacket) {
        ObjectNode jsonNode = Json.newObject();
        IpPacket.IpHeader header = ipPacket.getHeader();
        jsonNode.put("srcAddr", header.getSrcAddr().toString());
        jsonNode.put("dstAddr", header.getDstAddr().toString());
        jsonNode.put("protocol", header.getProtocol().name());
        jsonNode.put("version", header.getVersion().name());
        return jsonNode;
    }

    private JsonNode getTcpPacketMetrics(TcpPacket tcpPacket) {
        ObjectNode jsonNode = Json.newObject();
        jsonNode.put("srcPort", "" + tcpPacket.getHeader().getSrcPort());
        jsonNode.put("dstPort", "" + tcpPacket.getHeader().getDstPort());
        return jsonNode;
    }

    private JsonNode getUdpPacketMetrics(UdpPacket udpPacket) {
        ObjectNode jsonNode = Json.newObject();
        jsonNode.put("srcPort", "" + udpPacket.getHeader().getSrcPort());
        jsonNode.put("dstPort", "" + udpPacket.getHeader().getDstPort());
        return jsonNode;
    }

}
