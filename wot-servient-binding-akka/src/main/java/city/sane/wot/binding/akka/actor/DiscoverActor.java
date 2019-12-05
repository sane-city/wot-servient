package city.sane.wot.binding.akka.actor;

import akka.actor.*;
import akka.cluster.pubsub.DistributedPubSub;
import akka.cluster.pubsub.DistributedPubSubMediator;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import city.sane.akkamediator.MediatorActor;
import city.sane.akkamediator.relayextension.MediatorRelayExtension;
import city.sane.akkamediator.relayextension.MediatorRelayExtensionImpl;
import city.sane.wot.thing.Thing;
import city.sane.wot.thing.filter.ThingFilter;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

import static city.sane.wot.binding.akka.CrudMessages.RespondGetAll;

/**
 * This actor is temporarily created for a discovery process. The actor searches for the desired things, returns them, and then terminates itself.
 */
public class DiscoverActor extends AbstractActor {
    private final LoggingAdapter log = Logging.getLogger(getContext().getSystem(), this);
    private final Cancellable timer;
    private final ActorRef requester;
    private final ThingFilter filter;
    private final Map<String, Thing> things = new HashMap<>();

    public DiscoverActor(ActorRef requester, Duration timeout, ThingFilter filter) {
        this.requester = requester;
        this.filter = filter;

        this.timer = getContext()
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
        ActorRef mediator = null;
        try {
            mediator = MediatorRelayExtension.MediatorRelayExtensionProvider.get(getContext().getSystem()).mediator();
        } catch (MediatorRelayExtensionImpl.NoSuchMediatorException e) {
            mediator = getContext().getSystem().actorOf(MediatorActor.props(), "MediatorActor");
            MediatorRelayExtension.MediatorRelayExtensionProvider.get(getContext().getSystem()).register(getContext().getSystem().name(), mediator);
        }

        if (mediator == null) {
            log.error("Can not create Mediator!!!!!!");
        }

        MediatorRelayExtension.MediatorRelayExtensionProvider.get(getContext().getSystem()).join(getSelf());
        MediatorRelayExtension.MediatorRelayExtensionProvider.get(getContext().getSystem()).tell(ActorPath.fromString("bud://ALL/ALL"), new ThingsActor.Discover(filter));
    }

    @Override
    public void postStop() {
        log.info("Stopped");
        MediatorRelayExtension.MediatorRelayExtensionProvider.get(getContext().getSystem()).leave(getSelf());
        timer.cancel();
    }

    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .match(RespondGetAll.class, this::foundThings)
                .match(DiscoverTimeout.class, this::stop)
                .build();
    }

    private void foundThings(RespondGetAll<String, Thing> m) {
        log.info("Received {} thing(s) from {}", m.entities.size(), getSender());
        things.putAll(m.entities);
    }

    private void stop(DiscoverTimeout m) {
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
        final ActorRef requester;
        final Map<String, Thing> things;

        public Done(ActorRef requester, Map<String, Thing> things) {
            this.requester = requester;
            this.things = things;
        }
    }
}
