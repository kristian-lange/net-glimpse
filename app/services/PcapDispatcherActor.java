package services;

import akka.actor.ActorRef;
import akka.actor.Props;
import akka.actor.UntypedActor;
import com.fasterxml.jackson.databind.JsonNode;
import play.Logger;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by klange on 15.06.17.
 */
public class PcapDispatcherActor extends UntypedActor {

    private static final Logger.ALogger LOGGER = Logger.of(PcapDispatcherActor.class);

    public enum Protocol {REGISTER, UNREGISTER};

    private List<ActorRef> webSocketList = new ArrayList();

    public static Props props() {
        return Props.create(PcapDispatcherActor.class);
    }

    @Override
    public void onReceive(final Object msg) throws Throwable {
        if (msg instanceof JsonNode) {
            webSocketList.forEach(actorRef -> actorRef.tell(msg, self()));
        } else if (Protocol.REGISTER.equals(msg)) {
            webSocketList.add(sender());
            LOGGER.info("registered WebSocket actor");
        } else if (Protocol.UNREGISTER.equals(msg)) {
            webSocketList.remove(sender());
            LOGGER.info("unregistered WebSocket actor");
        } else {
            unhandled(msg);
        }
    }
}
