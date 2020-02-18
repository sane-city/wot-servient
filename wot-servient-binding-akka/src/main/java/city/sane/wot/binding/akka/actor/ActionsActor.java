package city.sane.wot.binding.akka.actor;

import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.actor.Props;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import city.sane.wot.thing.action.ExposedThingAction;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static city.sane.wot.binding.akka.actor.ThingsActor.Created;

/**
 * This Actor creates a {@link ActionActor} for each {@link ExposedThingAction}.
 */
class ActionsActor extends AbstractActor {
    private final LoggingAdapter log = Logging.getLogger(getContext().getSystem(), this);
    private final Map<String, ExposedThingAction<Object, Object>> actions;
    private final Set<ActorRef> children = new HashSet<>();

    private ActionsActor(Map<String, ExposedThingAction<Object, Object>> actions) {
        this.actions = actions;
    }

    @Override
    public void preStart() {
        log.debug("Started");

        if (!actions.isEmpty()) {
            actions.forEach((name, action) -> {
                ActorRef propertyActor = getContext().actorOf(ActionActor.props(name, action), name);
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
                .match(Created.class, m -> actionExposed())
                .build();
    }

    private void actionExposed() {
        if (children.remove(getSender()) && children.isEmpty()) {
            done();
        }
    }

    private void done() {
        log.debug("All actions have been exposed");
        getContext().getParent().tell(new Created<>(getSelf()), getSelf());
    }

    public static Props props(Map<String, ExposedThingAction<Object, Object>> properties) {
        return Props.create(ActionsActor.class, () -> new ActionsActor(properties));
    }
}