package services

import akka.actor.{Actor, ActorRef, Props}
import play.api.Logger
import play.api.libs.json.JsObject
import services.NifDispatcherActor.{Subscribe, Unsubscribe}

import scala.collection.mutable.ArrayBuffer

/**
  * Akka actor handling a single network interface and forwards all arriving
  * messages to all subscribing {@link WebSocketActor}. It also cares for a
  * WebSocket register where WebSocket actors can subscribe and unsubscribe.
  * <p>
  * It is created by the {@link PcapInitializer}.
  * <p>
  * Created by Kristian Lange on 2017.
  */
object NifDispatcherActor {

  def props(nifName: String) = Props(new NifDispatcherActor(nifName))

  case class Subscribe()

  case class Unsubscribe()

}

class NifDispatcherActor(nifName: String) extends Actor {

  val logger: Logger = Logger(this.getClass())

  var webSocketRegister: ArrayBuffer[ActorRef] = ArrayBuffer()

  def receive = {
    case msg: JsObject =>
      webSocketRegister.foreach(w => w ! msg)
    case Subscribe() =>
      webSocketRegister += sender()
      logger.info("subscribed WebSocket to network interface " + nifName)
    case Unsubscribe() =>
      webSocketRegister -= sender()
      logger.info("unsubscribed WebSocket from network interface " + nifName)
  }

}
