import com.google.inject.AbstractModule;
import play.libs.akka.AkkaGuiceSupport;
import services.PcapInitializer;
import services.WebSocketDispatcher;

/**
 * This class is a Guice module that tells Guice how to bind several
 * different types. This Guice module is created when the Play
 * application starts.
 *
 * Play will automatically use any class called `Module` that is in
 * the root package. You can create modules in other locations by
 * adding `play.modules.enabled` settings to the `application.conf`
 * configuration file.
 */
public class Module extends AbstractModule implements AkkaGuiceSupport {

    @Override
    protected void configure() {
        bind(PcapInitializer.class).asEagerSingleton();

        // Config which Akka actors should be handled by Guice
        bindActor(WebSocketDispatcher.class, "websocket-dispatcher-actor");
    }
}
