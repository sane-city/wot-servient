package city.sane.wot.binding.akka.actor;

import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.actor.Props;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import city.sane.Pair;
import city.sane.wot.thing.ExposedThing;

import java.util.HashSet;
import java.util.Set;

import static city.sane.wot.binding.akka.CrudMessages.Created;

/**
 * This Actor is responsible for the interaction with the respective Thing. It is started as soon as a thing is to be exposed and terminated when the thing
 * should no longer be exposed.<br>
 * For this purpose, the actuator creates a series of child actuators that allow interaction with a single
 * {@link city.sane.wot.thing.property.ExposedThingProperty}, {@link city.sane.wot.thing.action.ExposedThingAction}, or
 * {@link city.sane.wot.thing.event.ExposedThingEvent}.
 */
public class ThingActor extends AbstractActor {
    private final LoggingAdapter log = Logging.getLogger(getContext().getSystem(), this);
    private final ActorRef requestor;
    private final ExposedThing thing;
    private final Set<ActorRef> children = new HashSet<>();

    public ThingActor(ActorRef requester, ExposedThing thing) {
        this.requestor = requester;
        this.thing = thing;
    }

    @Override
    public void preStart() {
        log.info("Started");

        ActorRef allActor = getContext().actorOf(AllActor.props(thing), "all");
        children.add(allActor);

        ActorRef propertiesActor = getContext().actorOf(PropertiesActor.props(thing.getProperties()), "properties");
        children.add(propertiesActor);

        ActorRef actionsActor = getContext().actorOf(ActionsActor.props(thing.getActions()), "actions");
        children.add(actionsActor);

        ActorRef eventsActor = getContext().actorOf(EventsActor.props(thing.getEvents()), "events");
        children.add(eventsActor);
    }

    @Override
    public void postStop() {
        log.info("Stopped");
    }

    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .match(Created.class, this::created)
                .build();
    }

    private void created(Created m) {
        if (children.remove(getSender()) && children.isEmpty()) {
            log.info("Thing has been exposed");
            getContext().getParent().tell(new Created<>(new Pair<>(requestor, thing.getId())), getSelf());
        }
    }

    static public Props props(ActorRef requestor, ExposedThing thing) {
        return Props.create(ThingActor.class, () -> new ThingActor(requestor, thing));
    }
}
