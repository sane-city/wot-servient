package city.sane.wot.binding.akka;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import city.sane.RefCountResource;
import city.sane.RefCountResourceException;
import city.sane.wot.binding.ProtocolClient;
import city.sane.wot.binding.ProtocolClientFactory;
import city.sane.wot.binding.akka.actor.DiscoveryDispatcherActor;
import com.typesafe.config.Config;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;

import static java.util.concurrent.CompletableFuture.*;

/**
 * Creates new {@link AkkaProtocolClient} instances. An Actor System is created for this purpose.
 * The Actor System is intended for use in an
 * <a href="https://doc.akka.io/docs/akka/current/index-cluster.html">Akka Cluster</a> to discover
 * and interaction with other Actor Systems.<br> The Actor System can be configured via the
 * configuration parameter "wot.servient.akka" (see https://doc.akka.io/docs/akka/current/general/configuration.html).
 */
public class AkkaProtocolClientFactory implements ProtocolClientFactory {
    private static final Logger log = LoggerFactory.getLogger(AkkaProtocolClientFactory.class);
    private final RefCountResource<ActorSystem> actorSystemProvider;
    private ActorSystem system = null;
    private ActorRef discoveryActor = null;

    public AkkaProtocolClientFactory(Config config) {
        actorSystemProvider = SharedActorSystemProvider.singleton(config);
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
        try {
            system = actorSystemProvider.retain();
            return runAsync(() -> discoveryActor = system.actorOf(DiscoveryDispatcherActor.props(), "discovery-dispatcher"));
        }
        catch (RefCountResourceException e) {
            return failedFuture(e);
        }
    }

    @Override
    public CompletableFuture<Void> destroy() {
        log.debug("Terminate Actor System");

        if (system != null) {
            return runAsync(() -> {
                try {
                    actorSystemProvider.release();
                }
                catch (RefCountResourceException e) {
                    throw new CompletionException(e);
                }
            });
        }
        else {
            return completedFuture(null);
        }
    }

    public ActorSystem getActorSystem() {
        return system;
    }
}
