package city.sane.wot.binding.akka.actor;

import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.actor.Cancellable;
import akka.actor.Props;
import akka.cluster.pubsub.DistributedPubSub;
import akka.cluster.pubsub.DistributedPubSubMediator;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import city.sane.wot.binding.akka.Message;
import city.sane.wot.thing.Thing;
import city.sane.wot.thing.filter.ThingFilter;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import io.reactivex.rxjava3.core.Observer;

import java.time.Duration;
import java.util.Map;
import java.util.Objects;

/**
 * This actor is temporarily created for a discovery process. The actor searches for the desired
 * things, returns them, and then terminates itself.
 */
public class DiscoverActor extends AbstractActor {
    private final LoggingAdapter log = Logging.getLogger(getContext().getSystem(), this);
    private final Cancellable timer;
    private final ThingFilter filter;
    private final ActorRef mediator;
    private final Observer<Thing> subject;
    private final Duration timeout;

    public DiscoverActor(Observer<Thing> subject, ThingFilter filter, Duration timeout) {
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
            mediator.tell(new DistributedPubSubMediator.Publish(ThingsActor.TOPIC, new Discover(filter)), getSelf());
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
                .match(Discovered.class, this::things)
                .match(DiscoverTimeout.class, m -> timeout())
                .build();
    }

    private void things(Discovered m) {
        ActorRef sender = getSender();
        log.debug("Received Things message from {}", sender);

        Map<String, ? extends Thing> things = m.things;
        log.debug("Received {} thing(s) from {}. Send to subject", things.size(), sender);
        things.values().forEach(subject::onNext);
    }

    private void timeout() {
        log.debug("AkkaDiscovery timed out after {} second(s). Stop temporary discover actor.", timeout.toSeconds());
        getContext().stop(getSelf());
    }

    public static Props props(Observer<Thing> observer,
                              ThingFilter filter,
                              Duration timeout) {
        return Props.create(DiscoverActor.class, () -> new DiscoverActor(observer, filter, timeout));
    }

    public static class Discover implements Message {
        public final ThingFilter filter;

        public Discover(ThingFilter filter) {
            this.filter = filter;
        }

        Discover() {
            filter = null;
        }

        @Override
        public String toString() {
            return "Discover{" +
                    "filter=" + filter +
                    '}';
        }
    }

    public static class DiscoverFailed extends Message.ErrorMessage {
        public DiscoverFailed(Throwable e) {
            super(e);
        }
    }

    public static class Discovered implements Message {
        public final Map<String, ? extends Thing> things;

        public Discovered(Map<String, ? extends Thing> things) {
            this.things = things;
        }

        @Override
        public int hashCode() {
            return Objects.hash(things);
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }
            Discovered discovered = (Discovered) o;
            return Objects.equals(things, discovered.things);
        }

        @Override
        public String toString() {
            return "Discovered{" +
                    "things=" + things +
                    '}';
        }
    }

    // https://stackoverflow.com/a/53845446/1074188
    @JsonIgnoreProperties({ "hibernateLazyInitializer", "handler" })
    private static class DiscoverTimeout implements Message {
        public DiscoverTimeout() {
            // required by jackson
        }
    }
}