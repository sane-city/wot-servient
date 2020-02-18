package city.sane.wot.binding.akka.actor;

import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.actor.Props;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import city.sane.wot.binding.akka.Messages.Subscribe;
import city.sane.wot.binding.akka.Messages.SubscriptionComplete;
import city.sane.wot.binding.akka.Messages.SubscriptionError;
import city.sane.wot.binding.akka.Messages.SubscriptionNext;
import city.sane.wot.content.Content;
import city.sane.wot.content.ContentCodecException;
import city.sane.wot.content.ContentManager;
import city.sane.wot.thing.event.ExposedThingEvent;
import city.sane.wot.thing.form.Form;
import city.sane.wot.thing.form.Operation;

import static city.sane.wot.binding.akka.actor.ThingsActor.Created;

/**
 * This actor is responsible for the interaction with a {@link ExposedThingEvent}.
 */
class EventActor extends AbstractActor {
    private final LoggingAdapter log = Logging.getLogger(getContext().getSystem(), this);
    private final String name;
    private final ExposedThingEvent<Object> event;

    private EventActor(String name, ExposedThingEvent<Object> event) {
        this.name = name;
        this.event = event;
    }

    @Override
    public void preStart() {
        log.debug("Started");

        String href = getSelf().path().toStringWithAddress(getContext().getSystem().provider().getDefaultAddress());
        Form form = new Form.Builder()
                .setHref(href)
                .setContentType(ContentManager.DEFAULT)
                .setSubprotocol("longpoll")
                .setOp(Operation.SUBSCRIBE_EVENT)
                .build();

        event.addForm(form);
        log.debug("Assign '{}' to Event '{}'", href, name);

        getContext().getParent().tell(new Created<>(getSelf()), getSelf());
    }

    @Override
    public void postStop() {
        log.debug("Stopped");
    }

    @Override
    public AbstractActor.Receive createReceive() {
        return receiveBuilder()
                .match(Subscribe.class, m -> subscribe())
                .build();
    }

    private void subscribe() {
        ActorRef sender = getSender();
        log.debug("Received subscribe message from {}", sender);

        event.subscribe(
                next -> {
                    try {
                        Content content = ContentManager.valueToContent(next);
                        sender.tell(new SubscriptionNext(content), getSelf());
                    }
                    catch (ContentCodecException e) {
                        sender.tell(new SubscriptionError(e), getSelf());
                    }
                },
                e -> sender.tell(new SubscriptionError(e), getSelf()),
                () -> sender.tell(new SubscriptionComplete(), getSelf())
        );
    }

    public static Props props(String name, ExposedThingEvent<Object> event) {
        return Props.create(EventActor.class, () -> new EventActor(name, event));
    }
}