package city.sane.wot.binding.akka;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import city.sane.wot.binding.ProtocolClient;
import city.sane.wot.binding.ProtocolClientFactory;
import city.sane.wot.binding.akka.actor.DiscoveryDispatcherActor;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.CompletableFuture;

import static java.util.concurrent.CompletableFuture.completedFuture;
import static java.util.concurrent.CompletableFuture.runAsync;

/**
 * Creates new {@link AkkaProtocolClient} instances. An Actor System is created for this purpose.
 * The Actor System is intended for use in an
 * <a href="https://doc.akka.io/docs/akka/current/index-cluster.html">Akka Cluster</a> to discover
 * and interaction with other Actor Systems.<br> The Actor System can be configured via the
 * configuration parameter "wot.servient.akka" (see https://doc.akka.io/docs/akka/current/general/configuration.html).
 */
public class AkkaProtocolClientFactory implements ProtocolClientFactory {
    private static final Logger log = LoggerFactory.getLogger(AkkaProtocolClientFactory.class);
    private final SharedActorSystemProvider actorSystemProvider;
    private ActorSystem system = null;
    private ActorRef discoveryActor = null;

    public AkkaProtocolClientFactory(Config config) {
        String actorSystemName = config.getString("wot.servient.akka.system-name");
        Config actorSystemConfig = config.getConfig("wot.servient")
                .withFallback(ConfigFactory.load());
        actorSystemProvider = SharedActorSystemProvider.singleton(() -> ActorSystem.create(actorSystemName, actorSystemConfig));
    }

    @Override
    public String toString() {
        return "AkkaClient";
    }

    @Override
    public String getScheme() {
        return "akka";
    }

    @Override
    public ProtocolClient getClient() {
        return new AkkaProtocolClient(system, discoveryActor);
    }

    @Override
    public CompletableFuture<Void> init() {
        log.debug("Init Actor System");
        system = actorSystemProvider.create();

        return runAsync(() -> {
            discoveryActor = system.actorOf(DiscoveryDispatcherActor.props(), "discovery-dispatcher");
        });
    }

    @Override
    public CompletableFuture<Void> destroy() {
        log.debug("Terminate Actor System");

        if (system != null) {
            return actorSystemProvider.terminate();
        }
        else {
            return completedFuture(null);
        }
    }

    public ActorSystem getActorSystem() {
        return system;
    }
}
