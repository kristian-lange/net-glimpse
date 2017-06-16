package services;

import akka.actor.ActorRef;
import akka.actor.Props;
import akka.actor.UntypedActor;
import com.fasterxml.jackson.databind.JsonNode;

/**
 * Akka actor handling a single WebSocket. Sends messages to the WebSocket's webSocketOut, registers and unregisters itself to the NifDispatcherActor.
 * <p>
 * Created by klange on 15.06.17.
 */
public class WebSocketActor extends UntypedActor {

    private final ActorRef webSocketOut;
    private final ActorRef webSocketDispatcher;

    /**
     * Akka method to get this Actor started. Changes in props must be done in
     * the constructor too.
     */
    public static Props props(ActorRef out, ActorRef webSocketDispatcher) {
        return Props.create(WebSocketActor.class, out,
                webSocketDispatcher);
    }

    public WebSocketActor(ActorRef webSocketOut, ActorRef webSocketDispatcher) {
        this.webSocketOut = webSocketOut;
        this.webSocketDispatcher = webSocketDispatcher;
    }

    @Override
    public void preStart() {
        webSocketDispatcher.tell(NifDispatcherActor.Protocol.REGISTER, self());
    }

    @Override
    public void postStop() {
        webSocketDispatcher.tell(NifDispatcherActor.Protocol.UNREGISTER, self());
    }

    @Override
    public void onReceive(Object msg) throws Exception {
        if(msg instanceof JsonNode) {
            JsonNode jsonNode = (JsonNode) msg;
            webSocketOut.tell(jsonNode, self());
        } else {
            unhandled(msg);
        }
    }
}
