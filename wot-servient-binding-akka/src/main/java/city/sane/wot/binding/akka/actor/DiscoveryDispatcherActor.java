package city.sane.wot.binding.akka.actor;

import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.actor.Props;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import city.sane.wot.thing.Thing;
import city.sane.wot.thing.filter.ThingFilter;

import java.io.Serializable;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

import static city.sane.wot.binding.akka.CrudMessages.RespondGetAll;

/**
 * This Actor is started together with {@link city.sane.wot.binding.akka.AkkaProtocolClient} and is responsible for serving of discovery requests.
 * A {@link DiscoverActor} is created for each discovery request, which executes the actual discovery process and sends the result back to this actor.
 */
public class DiscoveryDispatcherActor extends AbstractActor {
    private final LoggingAdapter log = Logging.getLogger(getContext().getSystem(), this);

    @Override
    public void preStart() {
        log.info("Started");
    }

    @Override
    public void postStop() {
        log.info("Stopped");
    }

    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .match(Discover.class, this::startDiscovery)
                .match(DiscoverActor.Done.class, this::finishDiscovery)
                .build();
    }

    private void startDiscovery(Discover m) {
        log.info("Start discovery with filter '{}'", m.filter);

        getContext().actorOf(DiscoverActor.props(getSender(), Duration.ofSeconds(5), m.filter), "discovery");
    }

    private void finishDiscovery(DiscoverActor.Done m) {
        ActorRef requester = m.requester;
        Map<String, Thing> things = m.things;

        log.info("AkkaDiscovery finished. Send result requester '{}'", requester);
        requester.tell(new RespondGetAll<>(things), getSelf());
    }

    public static Props props() {
        return Props.create(DiscoveryDispatcherActor.class, DiscoveryDispatcherActor::new);
    }

    // CrudMessages
    public static class Discover implements Serializable {
        public final ThingFilter filter;

        public Discover(ThingFilter filter) {
            this.filter = filter;
        }
    }
}
