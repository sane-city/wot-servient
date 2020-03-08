package city.sane.wot.binding.akka.actor;

import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.actor.Props;
import akka.cluster.pubsub.DistributedPubSub;
import akka.cluster.pubsub.DistributedPubSubMediator;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import city.sane.wot.binding.akka.Message;
import city.sane.wot.binding.akka.Message.ErrorMessage;
import city.sane.wot.binding.akka.actor.DiscoverActor.Discover;
import city.sane.wot.binding.akka.actor.DiscoverActor.DiscoverFailed;
import city.sane.wot.binding.akka.actor.DiscoverActor.Discovered;
import city.sane.wot.content.Content;
import city.sane.wot.content.ContentCodecException;
import city.sane.wot.content.ContentManager;
import city.sane.wot.thing.ExposedThing;
import city.sane.wot.thing.Thing;
import city.sane.wot.thing.filter.ThingQueryException;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.stream.Collectors;

/**
 * This Actor is started together with {@link city.sane.wot.binding.akka.AkkaProtocolServer} and is
 * responsible for exposing things. For each exposed Thing a {@link ThingActor} is created, which is
 * responsible for the interaction with the Thing.
 */
public class ThingsActor extends AbstractActor {
    public static final String TOPIC = "thing-discovery";
    private final LoggingAdapter log = Logging.getLogger(getContext().getSystem(), this);
    private final Map<String, ExposedThing> things;
    private final Map<String, ActorRef> thingActors;
    private final Map<String, ActorRef> exposeRequesters;
    private final Map<String, ActorRef> destroyRequesters;
    private final ActorRef mediator;
    private final BiFunction<ActorContext, ExposedThing, ActorRef> thingActorCreator;
    private final BiConsumer<ActorContext, ActorRef> thingActorDestroyer;

    private ThingsActor(Map<String, ExposedThing> things,
                        Map<String, ActorRef> thingActors,
                        Map<String, ActorRef> exposeRequesters,
                        Map<String, ActorRef> destroyRequesters,
                        BiFunction<ActorContext, ExposedThing, ActorRef> thingActorCreator,
                        BiConsumer<ActorContext, ActorRef> thingActorDestroyer) {
        this.things = things;
        this.thingActors = thingActors;
        this.exposeRequesters = exposeRequesters;
        this.destroyRequesters = destroyRequesters;
        this.thingActorCreator = thingActorCreator;
        this.thingActorDestroyer = thingActorDestroyer;
        if (getContext().system().settings().config().getStringList("akka.extensions").contains("akka.cluster.pubsub.DistributedPubSub")) {
            mediator = DistributedPubSub.get(getContext().system()).mediator();
        }
        else {
            log.warning("DistributedPubSub extension missing. ANY Discovery via DistributedPubSub will not be supported.");
            mediator = null;
        }
    }

    @Override
    public void preStart() {
        log.debug("Started");

        if (mediator != null) {
            mediator.tell(new DistributedPubSubMediator.Subscribe(TOPIC, getSelf()), getSelf());
        }
    }

    @Override
    public void postStop() {
        log.debug("Stopped");

        if (mediator != null) {
            mediator.tell(new DistributedPubSubMediator.Unsubscribe(TOPIC, getSelf()), getSelf());
        }
    }

    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .match(DistributedPubSubMediator.SubscribeAck.class, this::subscriptionAcknowledged)
                .match(GetThings.class, m -> getThings())
                .match(Discover.class, this::discover)
                .match(Expose.class, this::expose)
                .match(Exposed.class, this::exposed)
                .match(Destroy.class, this::destroy)
                .match(Destroyed.class, this::destroyed)
                .build();
    }

    private void subscriptionAcknowledged(DistributedPubSubMediator.SubscribeAck m) {
        log.debug("Subscribed to topic '{}'", m.subscribe().topic());
    }

    private void getThings() {
        ActorRef sender = getSender();
        log.debug("Received GetThings message from {}", sender);

        try {
            Content content = ContentManager.valueToContent(things);
            sender.tell(new Things(content), getSelf());
        }
        catch (ContentCodecException e) {
            sender.tell(new GetThingsFailed(e), getSelf());
        }
    }

    private void discover(Discover m) {
        ActorRef sender = getSender();
        log.debug("Received Discover message from {}", sender);

        try {
            Collection<Thing> thingCollection = things.values().stream()
                    .map(t -> (Thing) t).collect(Collectors.toList());

            if (m.filter.getQuery() != null) {
                thingCollection = m.filter.getQuery().filter(thingCollection);
            }

            Map<String, Thing> thingsMap = thingCollection.stream().collect(Collectors.toMap(Thing::getId, t -> t));
            sender.tell(new Discovered(thingsMap), getSelf());
        }
        catch (ThingQueryException e) {
            sender.tell(new DiscoverFailed(e), getSelf());
        }
    }

    private void expose(Expose m) {
        ActorRef sender = getSender();
        log.debug("Received Expose message from {}", sender);

        String id = m.id;
        ExposedThing thing = things.get(id);

        if (thing != null) {
            // save requestor
            exposeRequesters.put(id, sender);

            // create thing actor
            ActorRef thingActor = thingActorCreator.apply(getContext(), thing);
            thingActors.put(id, thingActor);
        }
        else {
            log.warning("Thing with id {} not found", id);
            sender.tell(new ExposeFailed(new Exception("Thing with name " + id + " not found")), getSelf());
        }
    }

    private void exposed(Exposed m) {
        ActorRef sender = getSender();
        log.debug("Received Exposed message from {}", sender);

        String id = m.id;
        ActorRef requester = exposeRequesters.remove(id);

        if (requester != null) {
            log.debug("Inform requester that thing '{}' has been exposed '{}'", id, requester);
            requester.tell(m, getSelf());
        }
    }

    private void destroy(Destroy m) {
        ActorRef sender = getSender();
        log.debug("Received Destroy message from {}", sender);

        String id = m.id;

        // destroy thing
        ActorRef actorRef = thingActors.remove(id);

        if (actorRef != null) {
            // save requestor
            destroyRequesters.put(id, sender);

            log.debug("Destroy Thing '{}'. Stop Actor '{}'", id, actorRef);
            thingActorDestroyer.accept(getContext(), actorRef);
        }
    }

    private void destroyed(Destroyed m) {
        ActorRef sender = getSender();
        log.debug("Received Destroyed message from {}", sender);

        String id = m.id;
        ActorRef requester = destroyRequesters.remove(id);

        if (requester != null) {
            log.debug("Inform requester that thing '{}' has been destroyed '{}'", id, requester);
            requester.tell(m, getSelf());
        }
    }

    public static Props props(Map<String, ExposedThing> things) {
        return props(
                things,
                new HashMap<>(),
                new HashMap<>(),
                new HashMap<>(),
                (context, thing) -> context.actorOf(ThingActor.props(thing), thing.getId()),
                (context, actorRef) -> context.stop(actorRef)
        );
    }

    public static Props props(Map<String, ExposedThing> things,
                              Map<String, ActorRef> thingActors,
                              Map<String, ActorRef> exposeRequesters,
                              Map<String, ActorRef> destroyRequesters,
                              BiFunction<ActorContext, ExposedThing, ActorRef> thingActorProvider,
                              BiConsumer<ActorContext, ActorRef> thingActorDestroyer) {
        return Props.create(ThingsActor.class, () -> new ThingsActor(things, thingActors, exposeRequesters, destroyRequesters, thingActorProvider, thingActorDestroyer));
    }

    // https://stackoverflow.com/a/53845446/1074188
    @JsonIgnoreProperties({ "hibernateLazyInitializer", "handler" })
    public static class GetThings implements Message {
        public GetThings() {
            // required by jackson
        }

        @Override
        public String toString() {
            return "GetThings{}";
        }
    }

    public static class GetThingsFailed extends ErrorMessage {
        public GetThingsFailed(Throwable e) {
            super(e);
        }
    }

    public static class Things extends Message.ContentMessage {
        public Things(Content content) {
            super(content);
        }
    }

    public static class Expose implements Message {
        final String id;

        public Expose(String id) {
            this.id = id;
        }

        Expose() {
            id = null;
        }

        @Override
        public String toString() {
            return "Expose{" +
                    "id='" + id + '\'' +
                    '}';
        }
    }

    public static class ExposeFailed extends ErrorMessage {
        public ExposeFailed(Throwable e) {
            super(e);
        }
    }

    public static class Exposed implements Message {
        public final String id;

        public Exposed(String id) {
            this.id = id;
        }

        Exposed() {
            id = null;
        }

        @Override
        public String toString() {
            return "Exposed{" +
                    "id='" + id + '\'' +
                    '}';
        }
    }

    public static class Destroy implements Message {
        final String id;

        public Destroy(String id) {
            this.id = id;
        }

        Destroy() {
            id = null;
        }

        @Override
        public String toString() {
            return "Destroy{" +
                    "id='" + id + '\'' +
                    '}';
        }
    }

    public static class Destroyed implements Message {
        public final String id;

        Destroyed() {
            id = null;
        }

        public Destroyed(String id) {
            this.id = id;
        }

        @Override
        public String toString() {
            return "Destroyed{" +
                    "id='" + id + '\'' +
                    '}';
        }
    }
}