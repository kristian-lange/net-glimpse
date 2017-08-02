package services

import java.io.EOFException
import javax.inject.{Inject, Singleton}

import akka.actor.{ActorRef, ActorSystem}
import org.pcap4j.core._
import org.pcap4j.packet.{IpPacket, Packet, TcpPacket}
import play.Configuration
import play.api.Logger
import play.api.inject.ApplicationLifecycle

import scala.concurrent._
import ExecutionContext.Implicits.global
import scala.collection.mutable
import scala.concurrent.{Future, TimeoutException}

/**
  * Using Pcap4J (https://github.com/kaitoy/pcap4j) to access network
  * interfaces and forward some of the packages' data (metrics) to the
  * appropriate {@link NifDispatcherActor}.
  * <p>
  * Created by Kristian Lange on 2017.
  */
@Singleton
class PcapInitializer @Inject()(implicit actorSystem: ActorSystem,
                                configuration: Configuration,
                                lifecycle: ApplicationLifecycle) {

  val logger = Logger(this.getClass())

  /**
    * Default network interface (specified in application.conf)
    */
  private val defaultNifName = configuration.getString("nif", "empty")

  /**
    * If false net-glimpse filters out its own traffic
    * (specified in application.conf)
    */
  private val skipOwnTraffic = configuration.getBoolean("skipOwnTraffic", true)

  /**
    * IP / host the Play framework is  bound to (default 0.0.0.0)
    */
  private val httpAddress = configuration.getString("play.server.http.address")

  /**
    * Port the Play framework is bound to (default 9000)
    */
  private val httpPort = configuration.getInt("play.server.http.port")

  /**
    * Specifies the portion of the network packet to capture
    * https://serverfault.com/questions/253613
    */
  private val snaplen = configuration.getInt("snaplen", 65536)

  /**
    * Map: network interface name -> actor reference to {@link NifDispatcherActor}
    */
  val pcapActorRefMap: mutable.HashMap[String, ActorRef] = mutable.HashMap()

  def getPcapDispatcherActorRef(nifName: String = defaultNifName): ActorRef = {

    if (pcapActorRefMap.contains(nifName)) return pcapActorRefMap(nifName)

    val pcapDispatcherActorRef = actorSystem.actorOf(NifDispatcherActor.props(nifName))
    pcapActorRefMap += (nifName -> pcapDispatcherActorRef)

    val pcapHandle = openPcap(nifName, snaplen)
    pcapToDispatcher(pcapHandle, nifName)

    lifecycle.addStopHook {
      () => if (pcapHandle.isOpen()) Future.successful(pcapHandle.close()) else Future.successful({})
    }

    pcapDispatcherActorRef
  }

  def openPcap(networkInterfaceName: String, snaplen: Int): PcapHandle = try {
    val nif = Pcaps.getDevByName(networkInterfaceName)
    if (nif == null) throw new RuntimeException("Couldn't open network interface " + networkInterfaceName)
    else logger.info("Forward network traffic from " + nif.getName + "(" + nif.getAddresses + ")")
    nif.openLive(snaplen, PcapNetworkInterface.PromiscuousMode.PROMISCUOUS, 10)
  } catch {
    case e: PcapNativeException => {
      if (defaultNifName == "empty") logger.error("No network interface specified!")
      logger.error("Couldn't open network interface " + networkInterfaceName, e)
      throw new RuntimeException(e)
    }
  }

  def pcapToDispatcher(pcapHandle: PcapHandle, nifName: String) {
    // TODO Maybe there is a non-blocking way to get the packets
    Future {
      try
          while (true) try {
            val packet = pcapHandle.getNextPacketEx
            if (packet != null && (skipOwnTraffic && !isOurPacket(packet))) {
              val json = PacketToJsonTransfer.packageToJson(packet, pcapHandle.getTimestamp)
              pcapActorRefMap(nifName) ! json
            }
          } catch {
            case _: PcapNativeException | _: EOFException | _: TimeoutException => {}
          }
      catch {
        case e: NotOpenException => {}
      }
    }
  }

  /**
    * Check if this packet is one of our WebSocket packets: our WebSocket
    * packets are TCP packets that originate from our IP and port - or are
    * destined to our IP and port
    */
  def isOurPacket(packet: Packet): Boolean = {
    if (!packet.contains(classOf[IpPacket]) || !packet.contains(classOf[TcpPacket])) return false

    val ipHeader = packet.get(classOf[IpPacket]).getHeader
    val srcAddr = ipHeader.getSrcAddr.getHostAddress
    val dstAddr = ipHeader.getDstAddr.getHostAddress
    val tpcHeader = packet.get(classOf[TcpPacket]).getHeader
    val tcpSrcPort = tpcHeader.getSrcPort.valueAsInt
    val tcpDstPort = tpcHeader.getDstPort.valueAsInt

    // Check if our IP and port are equal to the packet's source IP and port
    // If our IP is 0.0.0.0 we listen on every IP
    if ((httpAddress == "0.0.0.0" || httpAddress == srcAddr) && httpPort == tcpSrcPort) return true

    // Check if our IP and port are equal to the packet's destination IP and port
    // If our IP is 0.0.0.0 we listen on every IP
    if ((httpAddress == "0.0.0.0" || httpAddress == dstAddr) && httpPort == tcpDstPort) return true

    false
  }

}
