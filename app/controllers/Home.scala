package controllers

import javax.inject._

import akka.actor.ActorSystem
import akka.stream.Materializer
import play.Configuration
import play.api.libs.json.JsValue
import play.api.libs.streams.ActorFlow
import play.api.mvc._
import services.{PcapInitializer, WebSocketActor}

/**
  * Created by Kristian Lange on 2017.
  */
@Singleton
class Home @Inject()(implicit actorSystem: ActorSystem,
                     materializer: Materializer,
                     configuration: Configuration,
                     pcapInitializer: PcapInitializer) extends Controller {

  /**
    * Default network interface (specified in application.conf)
    */
  private val defaultNifName = configuration.getString("nif", "empty")

  /**
    * This endpoint serves WebSockets that stream network header data
    *
    * @param nif Network interface name of the interface to be intercepted
    * @return WebSocket that streams network header data
    */
  def netdata(nif: String = defaultNifName) = WebSocket.accept[JsValue, JsValue] {
    _ => {
      val nifDispatcher =
        if (nif != null && nif.nonEmpty) pcapInitializer.getNifDispatcher(nif)
        else pcapInitializer.getNifDispatcher(defaultNifName)
      ActorFlow.actorRef(out => WebSocketActor.props(out, nifDispatcher))
    }
  }


}