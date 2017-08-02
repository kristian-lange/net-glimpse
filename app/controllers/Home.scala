package controllers

import javax.inject._

import akka.actor.ActorSystem
import akka.stream.Materializer
import play.api.libs.json.JsValue
import play.api.libs.streams.ActorFlow
import play.api.mvc._
import services.{PcapInitializer, WebSocketActor}

/**
  * Created by Kristian Lange on 2017.
  */
@Singleton
class Home @Inject()(implicit system: ActorSystem,
                     materializer: Materializer,
                     pcapInitializer: PcapInitializer) extends Controller {

  /**
    * Endpoint serves WebSockets that streams network header data
    *
    * @param nif Network interface name of the interface to be intercepted
    * @return WebSocket that streams network header data
    */
  def netdata(nif: String) = WebSocket.accept[JsValue, JsValue] { request =>
    val pcapDispatcherActorRef = pcapInitializer.getPcapDispatcherActorRef(nif)
    ActorFlow.actorRef(out => WebSocketActor.props(out, pcapDispatcherActorRef))
  }

}