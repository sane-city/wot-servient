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
import io.reactivex.rxjava3.subjects.PublishSubject;

import java.time.Duration;

/**
 * This actor is temporarily created for a discovery process. The actor searches for the desired
 * things, returns them, and then terminates itself.
 */
public class DiscoverActor extends AbstractActor {
    private final LoggingAdapter log = Logging.getLogger(getContext().getSystem(), this);
    private final Cancellable timer;
    private final ThingFilter filter;
    private final ActorRef mediator;
    private final PublishSubject<Thing> subject;
    private final Duration timeout;

    public DiscoverActor(PublishSubject<Thing> subject, ThingFilter filter, Duration timeout) {
        this.subject = subject;
        this.filter = filter;

        if (getContext().system().settings().config().getStringList("akka.extensions").contains("akka.cluster.pubsub.DistributedPubSub")) {
            mediator = DistributedPubSub.get(getContext().system()).mediator();
        }
        else {
            log.warning("DistributedPubSub extension missing. ANY Discovery via DistributedPubSub will not be supported.");
            mediator = null;
        }

        this.timeout = timeout;
        timer = getContext()
                .getSystem()
                .scheduler()
                .scheduleOnce(
                        this.timeout,
                        getSelf(),
                        new DiscoverTimeout(),
                        getContext().getDispatcher(),
                        getSelf());
    }

    @Override
    public void preStart() {
        log.debug("Started");

        if (mediator != null) {
            mediator.tell(new DistributedPubSubMediator.Publish(ThingsActor.TOPIC, new ThingsActor.Discover(filter)), getSelf());
        }
    }

    @Override
    public void postStop() {
        log.debug("Stopped. Complete subject");

        timer.cancel();
        subject.onComplete();
    }

    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .match(Things.class, this::foundThings)
                .match(DiscoverTimeout.class, m -> timeout())
                .build();
    }

    private void foundThings(Things m) {
        log.debug("Received {} thing(s) from {}. Send to subject", m.entities.size(), getSender());
        m.entities.values().forEach(subject::onNext);
    }

    private void timeout() {
        log.debug("AkkaDiscovery timed out after {} second(s). Stop temporary discover actor.", timeout.toSeconds());
        getContext().stop(getSelf());
    }

    public static Props props(PublishSubject<Thing> subject,
                              ThingFilter filter,
                              Duration timeout) {
        return Props.create(DiscoverActor.class, () -> new DiscoverActor(subject, filter, timeout));
    }

    public static class DiscoverTimeout {
        public DiscoverTimeout() {
            // required by jackson
        }
    }
}