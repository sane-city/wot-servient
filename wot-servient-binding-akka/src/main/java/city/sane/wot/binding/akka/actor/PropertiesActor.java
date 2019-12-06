package city.sane.wot.binding.akka.actor;

import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.actor.Props;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import city.sane.wot.thing.property.ExposedThingProperty;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static city.sane.wot.binding.akka.actor.ThingsActor.Created;

/**
 * This Actor creates a {@link PropertyActor} for each {@link ExposedThingProperty}.
 */
public class PropertiesActor extends AbstractActor {
    private final LoggingAdapter log = Logging.getLogger(getContext().getSystem(), this);
    private final Map<String, ExposedThingProperty> properties;
    private final Set<ActorRef> children = new HashSet<>();

    public PropertiesActor(Map<String, ExposedThingProperty> properties) {
        this.properties = properties;
    }

    @Override
    public void preStart() {
        log.info("Started");

        if (!properties.isEmpty()) {
            properties.forEach((name, property) -> {
                ActorRef propertyActor = getContext().actorOf(PropertyActor.props(name, property), name);
                children.add(propertyActor);
            });
        }
        else {
            done();
        }
    }

    @Override
    public void postStop() {
        log.info("Stopped");
    }

    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .match(Created.class, this::propertyExposed)
                .build();
    }

    private void propertyExposed(Created m) {
        if (children.remove(getSender()) && children.isEmpty()) {
            done();
        }
    }

    private void done() {
        log.info("All properties have been exposed");
        getContext().getParent().tell(new Created<>(getSelf()), getSelf());
    }

    public static Props props(Map<String, ExposedThingProperty> properties) {
        return Props.create(PropertiesActor.class, () -> new PropertiesActor(properties));
    }
}
