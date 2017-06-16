package services;

import akka.actor.ActorRef;
import akka.actor.Props;
import akka.actor.UntypedActor;
import com.fasterxml.jackson.databind.JsonNode;
import play.Logger;

import java.util.ArrayList;
import java.util.List;

/**
 * Akka actor handling a single network interface and forwards all arriving messages to all subscribing {@link WebSocketActor}. It also
 * cares for a WebSocket register where WebSocket actors can subscribe and unsubscribe.
 * <p>
 * It is created by the {@link PcapInitializer}.
 * <p>
 * Created by klange on 15.06.17.
 */
public class NifDispatcherActor extends UntypedActor {

    private static final Logger.ALogger LOGGER = Logger.of(NifDispatcherActor.class);

    public enum Protocol {REGISTER, UNREGISTER}

    private final String nifName;

    private List<ActorRef> webSocketRegister = new ArrayList();

    /**
     * Akka method to get this Actor started. Changes in props must be done in
     * the constructor too.
     */
    public static Props props(String nifName) {
        return Props.create(NifDispatcherActor.class, nifName);
    }

    public NifDispatcherActor(String nifName) {
        this.nifName = nifName;
    }

    @Override
    public void onReceive(final Object msg) throws Throwable {
        if(msg instanceof JsonNode) {
            webSocketRegister.forEach(actorRef -> actorRef.tell(msg, self()));
        } else if(Protocol.REGISTER.equals(msg)) {
            webSocketRegister.add(sender());
            LOGGER.info("registered WebSocket actor to network interface " + nifName);
        } else if(Protocol.UNREGISTER.equals(msg)) {
            webSocketRegister.remove(sender());
            LOGGER.info("unregistered WebSocket actor from network interface " + nifName);
        } else {
            unhandled(msg);
        }
    }
}
