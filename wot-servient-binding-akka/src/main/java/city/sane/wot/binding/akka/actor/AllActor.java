package city.sane.wot.binding.akka.actor;

import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.actor.Props;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import city.sane.wot.thing.ExposedThing;

import java.util.HashSet;
import java.util.Set;

/**
 * This Actor creates the {@link AllPropertiesActor} (and maybe more "all" actors in the future).
 */
class AllActor extends AbstractActor {
    private final LoggingAdapter log = Logging.getLogger(getContext().getSystem(), this);
    private final ExposedThing thing;
    private final Set<ActorRef> children = new HashSet<>();

    private AllActor(ExposedThing thing) {
        this.thing = thing;
    }

    @Override
    public void preStart() {
        log.info("Started");

        ActorRef propertiesActor = getContext().actorOf(AllPropertiesActor.props(thing), "properties");
        children.add(propertiesActor);
    }

    @Override
    public void postStop() {
        log.info("Stopped");
    }

    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .match(ThingsActor.Created.class, m -> exposed())
                .build();
    }

    private void exposed() {
        if (children.remove(getSender()) && children.isEmpty()) {
            done();
        }
    }

    private void done() {
        log.info("'all' resources have been exposed");
        getContext().getParent().tell(new ThingsActor.Created<>(getSelf()), getSelf());
    }

    public static Props props(ExposedThing thing) {
        return Props.create(AllActor.class, () -> new AllActor(thing));
    }
}
