package city.sane.wot.binding.akka.actor;

import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.actor.Props;
import akka.cluster.pubsub.DistributedPubSub;
import akka.cluster.pubsub.DistributedPubSubMediator;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import city.sane.Pair;
import city.sane.wot.content.Content;
import city.sane.wot.content.ContentCodecException;
import city.sane.wot.content.ContentManager;
import city.sane.wot.thing.ExposedThing;
import city.sane.wot.thing.Thing;
import city.sane.wot.thing.filter.ThingFilter;

import java.io.Serializable;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

import static city.sane.wot.binding.akka.Messages.Read;
import static city.sane.wot.binding.akka.Messages.RespondRead;

/**
 * This Actor is started together with {@link city.sane.wot.binding.akka.AkkaProtocolServer} and is responsible for exposing things. For each exposed Thing
 * a {@link ThingActor} is created, which is responsible for the interaction with the Thing.
 */
public class ThingsActor extends AbstractActor {
    public static final String TOPIC = "thing-discovery";

    private final LoggingAdapter log = Logging.getLogger(getContext().getSystem(), this);
    private final Map<String, ExposedThing> things;
    private final Map<String, ActorRef> children = new HashMap<>();
    private final ActorRef mediator;

    private ThingsActor(Map<String, ExposedThing> things) {
        this.things = things;
        if (getContext().system().settings().config().getStringList("wot.servient.akka.server.akka.extensions").contains("akka.cluster.pubsub.DistributedPubSub")) {
            mediator = DistributedPubSub.get(getContext().system()).mediator();
        }
        else {
            log.warning("DistributedPubSub extension missing. ANY Discovery will not be supported.");
            mediator = null;
        }
    }

    @Override
    public void preStart() {
        log.info("Started");

        if (mediator != null) {
            mediator.tell(new DistributedPubSubMediator.Subscribe(TOPIC, getSelf()), getSelf());
        }
    }

    @Override
    public void postStop() {
        log.info("Stopped");

        if (mediator != null) {
            mediator.tell(new DistributedPubSubMediator.Unsubscribe(TOPIC, getSelf()), getSelf());
        }
    }

    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .match(DistributedPubSubMediator.SubscribeAck.class, this::subscriptionAcknowledged)
                .match(Read.class, m -> getThings())
                .match(Discover.class, this::discover)
                .match(Expose.class, this::expose)
                .match(Created.class, this::exposed)
                .match(Destroy.class, this::destroy)
                .match(Deleted.class, this::destroyed)
                .build();
    }

    private void subscriptionAcknowledged(DistributedPubSubMediator.SubscribeAck m) {
        log.info("Subscribed to topic '{}'", m.subscribe().topic());
    }

    private void expose(Expose m) {
        String id = m.entity;
        ActorRef thingActor = getContext().actorOf(ThingActor.props(getSender(), things.get(id)), id);
        children.put(id, thingActor);
    }

    private void exposed(Created m) {
        Pair<ActorRef, String> pair = (Pair<ActorRef, String>) m.entity;
        ActorRef requester = pair.first();
        String id = pair.second();
        log.info("Thing '{}' has been exposed", id);
        requester.tell(new Created(getSender()), getSelf());
    }

    private void getThings() throws ContentCodecException {
        // TODO: We have to make Thing objects out of the ExposedThing objects, otherwise the Akka serializer will choke
        // on the Servient object. We take the detour via JSON strings. Maybe we just get the serializer to ignore the
        // service attribute?
        Map<String, Thing> thingMap = things.entrySet()
                .stream().collect(Collectors.toMap(Map.Entry::getKey, e -> Thing.fromJson(e.getValue().toJson())));
        Content content = ContentManager.valueToContent(thingMap);

        getSender().tell(
                new RespondRead(content),
                getSelf()
        );
    }

    private void discover(Discover m) {
        // TODO: We have to make Thing objects out of the ExposedThing objects, otherwise the Akka serializer will choke
        // on the Servient object. We take the detour via JSON strings. Maybe we just get the serializer to ignore the
        // service attribute?
        Collection<Thing> thingCollection = things.values().stream()
                .map(t -> Thing.fromJson(t.toJson())).collect(Collectors.toList());
        if (m.filter.getQuery() != null) {
            thingCollection = m.filter.getQuery().filter(thingCollection);
        }

        Map<String, Thing> thingsMap = thingCollection.stream().collect(Collectors.toMap(Thing::getId, t -> t));

        getSender().tell(
                new Things(thingsMap),
                getSelf()
        );
    }

    private void destroy(Destroy m) {
        String id = m.id;
        ActorRef actorRef = children.remove(id);
        if (actorRef != null) {
            log.info("Destroy Thing '{}'. Stop Actor '{}'", id, actorRef);
            getContext().stop(actorRef);
            getContext().watchWith(actorRef, new Deleted<>(new Pair<>(getSender(), id)));
        }
    }

    private void destroyed(Deleted m) {
        Pair<ActorRef, String> pair = (Pair<ActorRef, String>) m.id;
        ActorRef requester = pair.first();
        String id = pair.second();
        log.info("Thing '{}' is no longer exposed", id);
        requester.tell(new Deleted<>(getSender()), getSelf());
    }

    public static Props props(Map<String, ExposedThing> things) {
        return Props.create(ThingsActor.class, () -> new ThingsActor(things));
    }

    // CrudMessages
    public static class Discover implements Serializable {
        final ThingFilter filter;

        public Discover(ThingFilter filter) {
            this.filter = filter;
        }
    }

    public static class Things implements Serializable {
        public final Map<String, Thing> entities;

        public Things(Map<String, Thing> entities) {
            this.entities = entities;
        }
    }

    public static class Expose implements Serializable {
        final String entity;

        public Expose(String entity) {
            this.entity = entity;
        }
    }

    public static class Created<E extends Serializable> {
        public final E entity;

        public Created(E entity) {
            this.entity = entity;
        }
    }

    public static class Destroy implements Serializable {
        final String id;

        public Destroy(String id) {
            this.id = id;
        }
    }

    public static class Deleted<K extends Serializable> implements Serializable {
        public final K id;

        public Deleted(K id) {
            this.id = id;
        }
    }
}
