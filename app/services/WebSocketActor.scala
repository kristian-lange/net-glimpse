package services

import akka.actor._
import play.api.libs.json.JsObject

/**
  * Akka actor handling a single WebSocket. Sends messages to the WebSocket's
  * out, subscribes and unsubscribes itself to/from the [[NifDispatcherActor]].
  *
  * Created by Kristian Lange on 2017.
  */
object WebSocketActor {
  def props(out: ActorRef, nifDispatcherActor: ActorRef) =
    Props(new WebSocketActor(out, nifDispatcherActor))
}

class WebSocketActor(out: ActorRef, nifDispatcherActor: ActorRef) extends Actor {

  override def preStart() = {
    nifDispatcherActor ! NifDispatcherActor.Subscribe
  }

  override def postStop() = {
    nifDispatcherActor ! NifDispatcherActor.Unsubscribe
  }

  def receive = {
    case msg: JsObject =>
      out ! msg
  }
}
