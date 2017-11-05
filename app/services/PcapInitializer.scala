package services

import java.io.EOFException
import javax.inject.{Inject, Singleton}

import akka.actor.{ActorRef, ActorSystem}
import org.pcap4j.core._
import org.pcap4j.packet.{IpPacket, Packet, TcpPacket}
import play.api.{Configuration, Logger}
import play.api.inject.ApplicationLifecycle

import scala.collection.mutable
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.{Future, TimeoutException}

/**
  * Using Pcap4J (https://github.com/kaitoy/pcap4j) to access network
  * interfaces and forward packages' data (metrics) to the appropriate
  * [[NifDispatcherActor]].
  *
  * Created by Kristian Lange on 2017.
  */
@Singleton
class PcapInitializer @Inject()(implicit actorSystem: ActorSystem,
                                configuration: Configuration,
                                lifecycle: ApplicationLifecycle) {

  private val logger = Logger(this.getClass)

  /**
    * If false net-glimpse filters out its own traffic
    * (specified in application.conf)
    */
  private val skipOwnTraffic = configuration.get[Boolean]("skipOwnTraffic")

  /**
    * IP / host the Play framework is  bound to (default 0.0.0.0)
    */
  private val httpAddress = configuration.get[String]("play.server.http.address")

  /**
    * Port the Play framework is bound to (default 9000)
    */
  private val httpPort = configuration.get[Int]("play.server.http.port")

  /**
    * Specifies the portion of the network packet to capture
    * https://serverfault.com/questions/253613
    */
  private val snaplen = configuration.get[Int]("snaplen")

  /**
    * Map: network interface name -> actor reference to [[NifDispatcherActor]]
    */
  val nifDispatcherMap: mutable.HashMap[String, ActorRef] = mutable.HashMap()

  /**
    * @param nifName Name of the network interface to intercept
    * @return Returns an Option to the actor reference of the [[NifDispatcherActor]] that handles
    *         this nif
    */
  def getNifDispatcher(nifName: String): Option[ActorRef] = {

    // If it dispatcher exists already just return it
    if (nifDispatcherMap.contains(nifName)) return Some(nifDispatcherMap(nifName))

    // Get new dispatcher actor for this nif
    val pcapDispatcherActorRef = actorSystem.actorOf(NifDispatcherActor.props(nifName))
    nifDispatcherMap += (nifName -> pcapDispatcherActorRef)

    // Open pcap
    val pcapHandle = openPcap(nifName, snaplen).getOrElse(return None)

    // Send packets to dispatcher (do it async in parallel)
    Future {
      try {
        pcapToDispatcher(pcapHandle, nifName)
      } catch {
        case e: NotOpenException => // Do nothing
      }
    }

    // Close pcap when application stops
    lifecycle.addStopHook(() =>
      if (pcapHandle.isOpen) Future.successful(pcapHandle.close())
      else Future.successful({})
    )

    Some(pcapDispatcherActorRef)
  }

  private def openPcap(nifName: String, snaplen: Int): Option[PcapHandle] = {
    try {
      val nif = Pcaps.getDevByName(nifName)
      if (nif == null) {
        Logger.error("Couldn't open network interface " + nifName)
        return None
      }
      logger.info("Forward network traffic from " + nif.getName + "(" + nif.getAddresses + ")")
      Some(nif.openLive(snaplen, PcapNetworkInterface.PromiscuousMode.PROMISCUOUS, 10))
    } catch {
      case e: PcapNativeException =>
        if (nifName == "empty") logger.error("No network interface specified!")
        logger.error("Couldn't open network interface " + nifName, e)
        None
    }
  }

  private def pcapToDispatcher(pcapHandle: PcapHandle, nifName: String) {
    while (true) {
      try {
        val packet = pcapHandle.getNextPacketEx
        if (packet != null && (!skipOwnTraffic || !isOurPacket(packet))) {
          val json = PacketToJsonTransfer.packageToJson(packet, pcapHandle.getTimestamp)
          nifDispatcherMap(nifName) ! json
        }
      } catch {
        case _: PcapNativeException | _: EOFException | _: TimeoutException => // Just ignore and continue
      }
    }
  }

  /**
    * Check if this packet is one of our WebSocket packets: our WebSocket
    * packets are TCP packets that originate from our IP and port - or are
    * destined to our IP and port
    */
  private def isOurPacket(packet: Packet): Boolean = {
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
