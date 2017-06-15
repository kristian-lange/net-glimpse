package services;

import akka.actor.ActorRef;
import akka.actor.Props;
import akka.actor.UntypedActor;
import com.fasterxml.jackson.databind.JsonNode;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by klange on 15.06.17.
 */
public class WebSocketDispatcher extends UntypedActor {

    public enum Protocol {REGISTER, UNREGISTER};

    private List<ActorRef> webSocketList = new ArrayList();

    /**
     * Akka method to get this Actor started. Changes in props must be done in
     * the constructor too.
     */
    public static Props props() {
        return Props.create(WebSocketDispatcher.class);
    }

    public WebSocketDispatcher() {
    }

    @Override
    public void postStop() {
    }

    @Override
    public void onReceive(final Object msg) throws Throwable {
        if (msg instanceof JsonNode) {
            webSocketList.forEach(actorRef -> actorRef.tell(msg, self()));
        } else if (Protocol.REGISTER.equals(msg)) {
            webSocketList.add(sender());
        } else if (Protocol.UNREGISTER.equals(msg)) {
            webSocketList.remove(sender());
        } else {
            unhandled(msg);
        }
    }
}
