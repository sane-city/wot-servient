package city.sane.wot.binding.akka;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Terminated;
import city.sane.wot.binding.ProtocolClient;
import city.sane.wot.binding.ProtocolClientFactory;
import city.sane.wot.binding.akka.actor.DiscoveryDispatcherActor;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import scala.compat.java8.FutureConverters;

import java.util.concurrent.CompletableFuture;

/**
 * Creates new {@link AkkaProtocolClient} instances.
 * An Actor System is created for this purpose. The Actor System is intended for use in an
 * <a href="https://doc.akka.io/docs/akka/current/index-cluster.html">Akka Cluster</a> to discover and interaction with other Actor Systems.<br>
 * The Actor System can be configured via the configuration parameter "wot.servient.akka.client" (see
 * https://doc.akka.io/docs/akka/current/general/configuration.html).
 */
public class AkkaProtocolClientFactory implements ProtocolClientFactory {
    final static Logger log = LoggerFactory.getLogger(AkkaProtocolClientFactory.class);

    private final Config config;

    private ActorSystem system = null;
    private ActorRef discoveryActor = null;

    public AkkaProtocolClientFactory(Config config) {
        this.config = config;
    }

    @Override
    public String toString() {
        return "AkkaClient";
    }

    @Override
    public String getScheme() {
        return "akka.tcp";
    }

    @Override
    public ProtocolClient getClient() {
        return new AkkaProtocolClient(system, discoveryActor);
    }

    @Override
    public CompletableFuture<Void> init() {
        String actorSystemName = config.getString("wot.servient.akka.client.system-name");
        Config actorSystemConfig = config.getConfig("wot.servient.akka.client")
                .withFallback(ConfigFactory.defaultOverrides());

        log.debug("Create Actor System");
        system = ActorSystem.create(actorSystemName, actorSystemConfig);

        return CompletableFuture.runAsync(() -> {
            // wait a bit for the cluster to form
            try {
                Thread.sleep(3 * 1000);
            }
            catch (InterruptedException e) {
                e.printStackTrace();
            }

            discoveryActor = system.actorOf(DiscoveryDispatcherActor.props(), "discovery-dispatcher");
        });
    }

    @Override
    public CompletableFuture<Void> destroy() {
        log.debug("Terminate Actor System");
        CompletableFuture<Terminated> result = FutureConverters.toJava(system.terminate()).toCompletableFuture();
        return result.thenApply(terminated -> null);
    }
}
