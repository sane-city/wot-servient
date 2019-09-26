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

import static city.sane.wot.binding.akka.CrudMessages.Created;

/**
 * This Actor creates a {@link ActionActor} for each {@link ExposedThingAction}.
 */
public class ActionsActor extends AbstractActor {
    private final LoggingAdapter log = Logging.getLogger(getContext().getSystem(), this);
    private final Map<String, ExposedThingAction> actions;
    private final Set<ActorRef> children = new HashSet<>();

    public ActionsActor(Map<String, ExposedThingAction> actions) {
        this.actions = actions;
    }

    @Override
    public void preStart() {
        log.info("Started");

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
        log.info("Stopped");
    }

    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .match(Created.class, this::actionExposed)
                .build();
    }

    private void actionExposed(Created m) {
        if (children.remove(getSender()) && children.isEmpty()) {
            done();
        }
    }

    private void done() {
        log.info("All actions have been exposed");
        getContext().getParent().tell(new Created<>(getSelf()), getSelf());
    }

    static public Props props(Map<String, ExposedThingAction> properties) {
        return Props.create(ActionsActor.class, () -> new ActionsActor(properties));
    }
}