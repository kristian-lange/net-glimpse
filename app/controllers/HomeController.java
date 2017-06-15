package controllers;

import akka.actor.ActorRef;
import akka.actor.Props;
import com.fasterxml.jackson.databind.JsonNode;
import play.Logger;
import play.Logger.ALogger;
import play.mvc.Controller;
import play.mvc.LegacyWebSocket;
import play.mvc.Result;
import play.mvc.WebSocket;
import services.WebSocketActor;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

@Singleton
public class HomeController extends Controller {

	private static final ALogger LOGGER = Logger.of(HomeController.class);

	@Inject
	@Named("pcap-dispatcher-actor")
	private ActorRef pcapDispatcherActorRef;

	public Result index() {
		return ok(views.html.index.render());
	}

	public Result p5visu() {
		return ok(views.html.p5visu.render());
	}

	public LegacyWebSocket<JsonNode> ether() {
		return WebSocket.withActor(out -> WebSocketActor.props(out,
				pcapDispatcherActorRef));
	}

}
