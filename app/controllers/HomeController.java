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
import services.PcapInitializer;
import services.WebSocketActor;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

@Singleton
public class HomeController extends Controller {

	private static final ALogger LOGGER = Logger.of(HomeController.class);

	private final PcapInitializer pcapInitializer;

	@Inject
	public HomeController(PcapInitializer pcapInitializer) {
		this.pcapInitializer = pcapInitializer;
	}

	public Result index() {
		return ok(views.html.index.render());
	}

	public LegacyWebSocket<JsonNode> ether(String nif) {
		ActorRef pcapDispatcherActorRef = pcapInitializer.getPcapDispatcherActorRef(nif);
		return WebSocket.withActor(out -> WebSocketActor.props(out,
				pcapDispatcherActorRef));
	}

}
