package services;

import akka.actor.ActorRef;
import akka.actor.Props;
import akka.actor.UntypedActor;
import com.fasterxml.jackson.databind.JsonNode;

/**
 * Created by klange on 15.06.17.
 */
public class WebSocketActor extends UntypedActor {

    private final ActorRef out;
    private ActorRef webSocketDispatcher;

    /**
     * Akka method to get this Actor started. Changes in props must be done in
     * the constructor too.
     */
    public static Props props(ActorRef out, ActorRef webSocketDispatcher) {
        return Props.create(WebSocketActor.class, out,
                webSocketDispatcher);
    }

    public WebSocketActor(ActorRef out, ActorRef webSocketDispatcher) {
        this.out = out;
        this.webSocketDispatcher = webSocketDispatcher;
    }

    @Override
    public void preStart() {
        webSocketDispatcher.tell(PcapDispatcherActor.Protocol.REGISTER, self());
    }

    @Override
    public void postStop() {
        webSocketDispatcher.tell(PcapDispatcherActor.Protocol.UNREGISTER, self());
    }

    @Override
    public void onReceive(Object msg) throws Exception {
        if (msg instanceof JsonNode) {
            JsonNode jsonNode = (JsonNode) msg;
            out.tell(jsonNode, self());
        } else {
            unhandled(msg);
        }
    }
}
