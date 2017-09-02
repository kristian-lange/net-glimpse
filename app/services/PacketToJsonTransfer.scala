package services

import java.sql.Timestamp

import org.pcap4j.packet._
import play.api.libs.json.{JsString, JsValue, Json}

/**
  * Utility class that extracts data from network packets and puts them into JSON
  *
  * Created by Kristian Lange on 2017.
  */
object PacketToJsonTransfer {

  def packageToJson(packet: Packet, timestamp: Timestamp): JsValue = {
    var json = Json.obj()
    json += ("timestamp", JsString(timestamp.toString))
    if (packet.contains(classOf[EthernetPacket]))
      json += ("ethernet", getEthernetPacketMetrics(packet.get(classOf[EthernetPacket])))
    if (packet.contains(classOf[ArpPacket]))
      json += ("arp", getArpPacketMetrics(packet.get(classOf[ArpPacket])))
    if (packet.contains(classOf[IpPacket]))
      json += ("ip", getIpPacketMetrics(packet.get(classOf[IpPacket])))
    if (packet.contains(classOf[TcpPacket]))
      json += ("tcp", getTcpPacketMetrics(packet.get(classOf[TcpPacket])))
    if (packet.contains(classOf[UdpPacket]))
      json += ("udp", getUdpPacketMetrics(packet.get(classOf[UdpPacket])))
    json
  }

  private def getEthernetPacketMetrics(ethernetPacket: EthernetPacket) = Json.obj(
    "macSrcAddr" -> ethernetPacket.getHeader.getSrcAddr.toString,
    "macDstAddr" -> ethernetPacket.getHeader.getDstAddr.toString,
    "etherType" -> ethernetPacket.getHeader.getType.name
  )

  private def getArpPacketMetrics(arpPacket: ArpPacket) = Json.obj(
    "srcHardwareAddr" -> arpPacket.getHeader.getSrcHardwareAddr.toString,
    "srcProtocolAddr" -> arpPacket.getHeader.getSrcProtocolAddr.toString,
    "dstHardwareAddr" -> arpPacket.getHeader.getDstHardwareAddr.toString,
    "dstProtocolAddr" -> arpPacket.getHeader.getDstProtocolAddr.toString,
    "hardwareType" -> arpPacket.getHeader.getHardwareType.name,
    "operation" -> arpPacket.getHeader.getOperation.name,
    "protocolType" -> arpPacket.getHeader.getProtocolType.name
  )

  private def getIpPacketMetrics(ipPacket: IpPacket) = Json.obj(
    "srcAddr" -> ipPacket.getHeader.getSrcAddr.getHostAddress,
    "dstAddr" -> ipPacket.getHeader.getDstAddr.getHostAddress,
    "dstIsMc" -> ipPacket.getHeader.getDstAddr.isMulticastAddress,
    "protocol" -> ipPacket.getHeader.getProtocol.name,
    "version" -> ipPacket.getHeader.getVersion.name
  )

  private def getTcpPacketMetrics(tcpPacket: TcpPacket) = Json.obj(
    "srcPort" -> tcpPacket.getHeader.getSrcPort.valueAsInt,
    "srcPortName" -> tcpPacket.getHeader.getSrcPort.name,
    "dstPort" -> tcpPacket.getHeader.getDstPort.valueAsInt,
    "dstPortName" -> tcpPacket.getHeader.getDstPort.name
  )

  private def getUdpPacketMetrics(udpPacket: UdpPacket) = Json.obj(
    "srcPort" -> udpPacket.getHeader.getSrcPort.valueAsInt,
    "srcPortName" -> udpPacket.getHeader.getSrcPort.name,
    "dstPort" -> udpPacket.getHeader.getDstPort.valueAsInt,
    "dstPortName" -> udpPacket.getHeader.getDstPort.name
  )

}
