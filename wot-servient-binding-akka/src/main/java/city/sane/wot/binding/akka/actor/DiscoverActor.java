package city.sane.wot.binding.akka.actor;

import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.actor.Cancellable;
import akka.actor.Props;
import akka.cluster.pubsub.DistributedPubSub;
import akka.cluster.pubsub.DistributedPubSubMediator;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import city.sane.wot.binding.akka.actor.ThingsActor.Things;
import city.sane.wot.thing.Thing;
import city.sane.wot.thing.filter.ThingFilter;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

/**
 * This actor is temporarily created for a discovery process. The actor searches for the desired things, returns them, and then terminates itself.
 */
public class DiscoverActor extends AbstractActor {
    private final LoggingAdapter log = Logging.getLogger(getContext().getSystem(), this);
    private final Cancellable timer;
    private final ActorRef requester;
    private final ThingFilter filter;
    private final ActorRef mediator;
    private final Map<String, Thing> things = new HashMap<>();

    private DiscoverActor(ActorRef requester, Duration timeout, ThingFilter filter) {
        this.requester = requester;
        this.filter = filter;
        if (getContext().system().settings().config().getStringList("wot.servient.akka.server.akka.extensions").contains("akka.cluster.pubsub.DistributedPubSub")) {
            mediator = DistributedPubSub.get(getContext().system()).mediator();
        }
        else {
            log.warning("DistributedPubSub extension missing. ANY Discovery will not be supported.");
            mediator = null;
        }

        timer = getContext()
                .getSystem()
                .scheduler()
                .scheduleOnce(
                        timeout,
                        getSelf(),
                        new DiscoverTimeout(),
                        getContext().getDispatcher(),
                        getSelf());
    }

    @Override
    public void preStart() {
        log.info("Started");

        if (mediator != null) {
            mediator.tell(new DistributedPubSubMediator.Publish(ThingsActor.TOPIC, new ThingsActor.Discover(filter)), getSelf());
        }
    }

    @Override
    public void postStop() {
        log.info("Stopped");

        timer.cancel();
    }

    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .match(Things.class, this::foundThings)
                .match(DiscoverTimeout.class, m -> stop())
                .build();
    }

    private void foundThings(Things m) {
        log.info("Received {} thing(s) from {}", m.entities.size(), getSender());
        things.putAll(m.entities);
    }

    private void stop() {
        log.info("AkkaDiscovery timed out. Send all Things collected so far to parent");
        getContext().getParent().tell(new Done(requester, things), getSelf());
        getContext().stop(getSelf());
    }

    public static Props props(ActorRef requester, Duration timeout, ThingFilter filter) {
        return Props.create(DiscoverActor.class, () -> new DiscoverActor(requester, timeout, filter));
    }

    // CrudMessages
    public static class DiscoverTimeout {

    }

    public static class Done {
        public final ActorRef requester;
        public final Map<String, Thing> things;

        public Done(ActorRef requester, Map<String, Thing> things) {
            this.requester = requester;
            this.things = things;
        }
    }
}
