package city.sane.wot.binding.akka;

import akka.actor.ActorRef;
import akka.actor.ActorSelection;
import akka.actor.ActorSystem;
import city.sane.wot.binding.ProtocolClient;
import city.sane.wot.binding.ProtocolClientException;
import city.sane.wot.binding.akka.actor.DiscoveryDispatcherActor;
import city.sane.wot.content.Content;
import city.sane.wot.thing.Thing;
import city.sane.wot.thing.filter.ThingFilter;
import city.sane.wot.thing.form.Form;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.Collection;
import java.util.concurrent.CompletableFuture;

import static akka.pattern.Patterns.ask;
import static city.sane.wot.binding.akka.Messages.*;
import static city.sane.wot.binding.akka.actor.ThingsActor.Things;

/**
 * Allows consuming Things via Akka Actors.<br>
 * The Actor System created by {@link AkkaProtocolClientFactory} is used for this purpose and thus enables interaction with exposed Things on other actuator
 * systems.
 */
public class AkkaProtocolClient implements ProtocolClient {
    static final Logger log = LoggerFactory.getLogger(AkkaProtocolClient.class);

    private final ActorSystem system;
    private final ActorRef discoveryActor;

    public AkkaProtocolClient(ActorSystem system, ActorRef discoveryActor) {
        this.system = system;
        this.discoveryActor = discoveryActor;
    }

    @Override
    public CompletableFuture<Content> readResource(Form form) {
        Read message = new Read();
        String href = form.getHref();
        if (href == null) {
            return CompletableFuture.failedFuture(new ProtocolClientException("no href given"));
        }
        log.debug("AkkaClient sending '{}' to {}", message, href);

        ActorSelection selection = system.actorSelection(href);
        Duration timeout = Duration.ofSeconds(10);
        return ask(selection, message, timeout)
                .thenApply(m -> ((RespondRead) m).content)
                .toCompletableFuture();
    }

    @Override
    public CompletableFuture<Content> writeResource(Form form, Content content) {
        Write message = new Write(content);
        String href = form.getHref();
        if (href == null) {
            return CompletableFuture.failedFuture(new ProtocolClientException("no href given"));
        }
        log.debug("AkkaClient sending '{}' to {}", message, href);

        ActorSelection selection = system.actorSelection(href);
        Duration timeout = Duration.ofSeconds(10);
        return ask(selection, message, timeout)
                .thenApply(m -> ((Written) m).content)
                .toCompletableFuture();
    }

    @Override
    public CompletableFuture<Collection<Thing>> discover(ThingFilter filter) {
        DiscoveryDispatcherActor.Discover message = new DiscoveryDispatcherActor.Discover(filter);
        log.debug("AkkaClient sending '{}' to {}", message, discoveryActor);

        Duration timeout = Duration.ofSeconds(10);
        return ask(discoveryActor, message, timeout)
                .thenApply(m -> ((Things) m).entities.values())
                .toCompletableFuture();
    }
}
