package city.sane.wot.binding.akka;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import city.sane.wot.binding.ProtocolServer;
import city.sane.wot.binding.ProtocolServerException;
import city.sane.wot.binding.akka.actor.ThingsActor;
import city.sane.wot.thing.ExposedThing;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import scala.compat.java8.FutureConverters;

import java.net.URI;
import java.net.URISyntaxException;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * Allows exposing Things via Akka Actors.<br>
 * Starts an Actor System with an {@link ThingsActor} actuator. This Actuator is responsible for exposing Things. The Actor System is intended for use in
 * an <a href="https://doc.akka.io/docs/akka/current/index-cluster.html">Akka Cluster</a> to discover and interact with other Actuator Systems.<br>
 * The Actor System can be configured via the configuration parameter "wot.servient.akka.server" (see
 * https://doc.akka.io/docs/akka/current/general/configuration.html).
 */
public class AkkaProtocolServer implements ProtocolServer {
    private static final Logger log = LoggerFactory.getLogger(AkkaProtocolServer.class);

    private final Map<String, ExposedThing> things = new HashMap<>();
    private final String actorSystemName;
    private final Config actorSystemConfig;
    private final AkkaProtocolPattern pattern;
    private ActorSystem system;
    private ActorRef thingsActor;

    public AkkaProtocolServer(Config config) {
        this(
                config.getString("wot.servient.akka.server.system-name"),
                config.getConfig("wot.servient.akka.server").withFallback(ConfigFactory.defaultOverrides()),
                new AkkaProtocolPattern()
        );
    }

    AkkaProtocolServer(String actorSystemName, Config actorSystemConfig, AkkaProtocolPattern pattern) {
        this.actorSystemName = actorSystemName;
        this.actorSystemConfig = actorSystemConfig;
        this.pattern = pattern;
    }

    @Override
    public String toString() {
        return "AkkaServer";
    }

    @Override
    public CompletableFuture<Void> start() {
        log.info("Start AkkaServer");

        if (system == null) {
            system = ActorSystem.create(actorSystemName, actorSystemConfig);

            thingsActor = system.actorOf(ThingsActor.props(things), "things");
        }

        return CompletableFuture.completedFuture(null);
    }

    @Override
    public CompletableFuture<Void> stop() {
        log.info("Stop AkkaServer");

        if (system != null) {
            return FutureConverters.toJava(system.terminate()).toCompletableFuture().thenApply(r -> null);
        }
        else {
            return CompletableFuture.completedFuture(null);
        }
    }

    @Override
    public CompletableFuture<Void> expose(ExposedThing thing) {
        log.info("AkkaServer exposes '{}'", thing.getTitle());

        if (system == null) {
            return CompletableFuture.failedFuture(new ProtocolServerException("Unable to expose thing before AkkaServer has been started"));
        }

        things.put(thing.getId(), thing);

        Duration timeout = Duration.ofSeconds(10);
        return pattern.ask(thingsActor, new ThingsActor.Expose(thing.getId()), timeout)
                .thenApply(m -> {
                    ActorRef thingActor = (ActorRef) ((ThingsActor.Created) m).entity;
                    String endpoint = thingActor.path().toStringWithAddress(system.provider().getDefaultAddress());
                    log.info("AkkaServer has '{}' exposed at {}", thing.getId(), endpoint);
                    return (Void) null;
                }).toCompletableFuture();
    }

    @Override
    public CompletableFuture<Void> destroy(ExposedThing thing) {
        log.info("AkkaServer stop exposing '{}'", thing.getTitle());

        if (system == null) {
            return CompletableFuture.completedFuture(null);
        }

        if (things.remove(thing.getId()) == null) {
            return CompletableFuture.completedFuture(null);
        }

        Duration timeout = Duration.ofSeconds(10);
        return pattern.ask(thingsActor, new ThingsActor.Destroy(thing.getId()), timeout)
                .thenApply(m -> {
                    ActorRef thingActor = (ActorRef) ((ThingsActor.Deleted) m).id;
                    String endpoint = thingActor.path().toStringWithAddress(system.provider().getDefaultAddress());
                    log.info("AkkaServer does not expose more '{}' at {}", thing.getId(), endpoint);
                    return (Void) null;
                }).toCompletableFuture();
    }

    @Override
    public URI getDirectoryUrl() {
        try {
            String endpoint = thingsActor.path().toStringWithAddress(system.provider().getDefaultAddress());
            return new URI(endpoint);
        }
        catch (URISyntaxException e) {
            log.warn("Unable to create directory url", e);
            return null;
        }
    }

    @Override
    public URI getThingUrl(String id) {
        try {
            String endpoint = thingsActor.path().child(id).toStringWithAddress(system.provider().getDefaultAddress());
            return new URI(endpoint);
        }
        catch (URISyntaxException e) {
            log.warn("Unable to create thing url", e);
            return null;
        }
    }

    public ActorSystem getActorSystem() {
        return system;
    }
}