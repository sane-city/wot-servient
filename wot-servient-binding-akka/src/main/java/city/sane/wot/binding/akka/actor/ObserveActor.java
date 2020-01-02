package city.sane.wot.binding.akka.actor;

import akka.actor.AbstractActor;
import akka.actor.ActorSelection;
import akka.actor.Props;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import city.sane.wot.binding.akka.Messages.Subscribe;
import city.sane.wot.binding.akka.Messages.SubscriptionComplete;
import city.sane.wot.binding.akka.Messages.SubscriptionError;
import city.sane.wot.binding.akka.Messages.SubscriptionNext;
import city.sane.wot.content.Content;
import city.sane.wot.thing.observer.Observer;

public class ObserveActor extends AbstractActor {
    private final LoggingAdapter log = Logging.getLogger(getContext().getSystem(), this);
    private final Observer<Content> observer;
    private final ActorSelection selection;

    public ObserveActor(Observer<Content> observer, ActorSelection selection) {
        this.observer = observer;
        this.selection = selection;
    }

    @Override
    public void preStart() {
        log.info("Started");

        log.debug("Send Subscribe message to {}", selection);
        selection.tell(new Subscribe(), getSelf());
    }

    @Override
    public void postStop() {
        log.info("Stop");
    }

    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .match(SubscriptionNext.class, m -> observer.next(m.next))
                .match(SubscriptionError.class, m -> {
                    observer.error(m.e);
                    getContext().stop(getSelf());
                })
                .match(SubscriptionComplete.class, m -> {
                    observer.complete();
                    getContext().stop(getSelf());
                })
                .build();
    }

    public static Props props(Observer<Content> observer, ActorSelection selection) {
        return Props.create(ObserveActor.class, () -> new ObserveActor(observer, selection));
    }

}
