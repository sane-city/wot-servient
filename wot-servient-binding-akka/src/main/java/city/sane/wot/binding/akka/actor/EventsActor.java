package city.sane.wot.binding.akka.actor;

import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.actor.Props;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import city.sane.wot.thing.event.ExposedThingEvent;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static city.sane.wot.binding.akka.actor.ThingsActor.Created;

/**
 * This Actor creates a {@link EventActor} for each {@link ExposedThingEvent}.
 */
class EventsActor extends AbstractActor {
    private final LoggingAdapter log = Logging.getLogger(getContext().getSystem(), this);
    private final Map<String, ExposedThingEvent> events;
    private final Set<ActorRef> children = new HashSet<>();

    private EventsActor(Map<String, ExposedThingEvent> events) {
        this.events = events;
    }

    @Override
    public void preStart() {
        log.debug("Started");

        if (!events.isEmpty()) {
            events.forEach((name, event) -> {
                ActorRef propertyActor = getContext().actorOf(EventActor.props(name, event), name);
                children.add(propertyActor);
            });
        }
        else {
            done();
        }
    }

    @Override
    public void postStop() {
        log.debug("Stopped");
    }

    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .match(Created.class, m -> eventExposed())
                .build();
    }

    private void eventExposed() {
        if (children.remove(getSender()) && children.isEmpty()) {
            done();
        }
    }

    private void done() {
        log.debug("All events have been exposed");
        getContext().getParent().tell(new Created<>(getSelf()), getSelf());
    }

    public static Props props(Map<String, ExposedThingEvent> properties) {
        return Props.create(EventsActor.class, () -> new EventsActor(properties));
    }
}