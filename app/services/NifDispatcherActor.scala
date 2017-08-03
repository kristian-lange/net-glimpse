package services

import akka.actor.{Actor, ActorRef, Props}
import play.api.Logger
import play.api.libs.json.JsObject
import services.NifDispatcherActor.{Subscribe, Unsubscribe}

import scala.collection.mutable.ArrayBuffer

/**
  * Akka actor handling a single network interface and forwards all arriving
  * messages to all subscribing [[WebSocketActor]]. It also cares for a
  * WebSocket register where WebSocket actors can subscribe and unsubscribe.
  *
  * A new NifDispatcherActor is created by the [[PcapInitializer]].
  *
  * Created by Kristian Lange on 2017.
  */
object NifDispatcherActor {

  def props(nifName: String) = Props(new NifDispatcherActor(nifName))

  case object Subscribe

  case object Unsubscribe

}

class NifDispatcherActor(nifName: String) extends Actor {

  private val logger: Logger = Logger(this.getClass)

  private val webSocketRegister: ArrayBuffer[ActorRef] = ArrayBuffer()

  def receive = {
    case msg: JsObject =>
      webSocketRegister.foreach(ws => ws ! msg)
    case Subscribe =>
      webSocketRegister += sender
      logger.info("subscribed WebSocket to network interface " + nifName)
    case Unsubscribe =>
      webSocketRegister -= sender
      logger.info("unsubscribed WebSocket from network interface " + nifName)
  }

}
