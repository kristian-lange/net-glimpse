package controllers

import javax.inject._

import akka.actor.ActorSystem
import akka.stream.Materializer
import play.api.Configuration
import play.api.libs.json.JsValue
import play.api.libs.streams.ActorFlow
import play.api.mvc._
import services.{PcapInitializer, WebSocketActor}

import scala.concurrent.Future

/**
  * Created by Kristian Lange on 2017.
  */
@Singleton
class Home @Inject()(implicit actorSystem: ActorSystem,
                     materializer: Materializer,
                     configuration: Configuration,
                     pcapInitializer: PcapInitializer,
                     controllerComponents: ControllerComponents)
    extends AbstractController(controllerComponents) {

  /**
    * Default network interface (specified in application.conf)
    */
  private val defaultNifName = configuration.get[String]("nif")

  /**
    * This endpoint serves WebSockets that stream network header data
    *
    * @param nif Network interface name of the interface to be intercepted
    * @return WebSocket that streams network header data or 'forbidden' in case something went wrong
    */
  def netdata(nif: String = defaultNifName): WebSocket = WebSocket.acceptOrResult[JsValue, JsValue] {
    _ => {
      val nifDispatcherOption =
        if (nif != null && nif.nonEmpty) pcapInitializer.getNifDispatcher(nif)
        else pcapInitializer.getNifDispatcher(defaultNifName)

      Future.successful(nifDispatcherOption match {
        case None => Left(Forbidden)
        case Some(nifDispatcher) => Right(ActorFlow.actorRef(out => WebSocketActor.props(out,
          nifDispatcher)))
      })
    }
  }


}
