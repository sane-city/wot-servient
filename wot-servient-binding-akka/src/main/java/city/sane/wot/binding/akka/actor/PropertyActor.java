package city.sane.wot.binding.akka.actor;

import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.actor.Props;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import city.sane.akkamediator.MediatorActor;
import city.sane.wot.content.Content;
import city.sane.wot.content.ContentCodecException;
import city.sane.wot.content.ContentManager;
import city.sane.wot.thing.form.Form;
import city.sane.wot.thing.form.Operation;
import city.sane.wot.thing.property.ExposedThingProperty;

import static city.sane.wot.binding.akka.Messages.*;
import static city.sane.wot.binding.akka.actor.ThingsActor.Created;

/**
 * This actor is responsible for the interaction with a {@link ExposedThingProperty}.
 */
class PropertyActor extends AbstractActor {
    private final LoggingAdapter log = Logging.getLogger(getContext().getSystem(), this);
    private final String name;
    private final ExposedThingProperty property;

    private PropertyActor(String name, ExposedThingProperty property) {
        this.name = name;
        this.property = property;
    }

    @Override
    public void preStart() {
        log.info("Started");

        String href = MediatorActor.remoteOverlayPath(getSelf().path()).toString();
        Form.Builder builder = new Form.Builder()
                .setHref(href)
                .setContentType(ContentManager.DEFAULT);
        if (!property.isWriteOnly()) {
            builder.setOp(Operation.READ_PROPERTY);
        }
        if (!property.isReadOnly()) {
            builder.addOp(Operation.WRITE_PROPERTY);
        }
        if (property.isObservable()) {
            builder.addOp(Operation.OBSERVE_PROPERTY);
        }

        property.addForm(builder.build());
        log.info("Assign '{}' to Property '{}'", href, name);

        getContext().getParent().tell(new Created<>(getSelf()), getSelf());
    }

    @Override
    public void postStop() {
        log.info("Stopped");
    }

    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .match(Read.class, m -> read())
                .match(Write.class, this::write)
                .match(Subscribe.class, m -> subscribe())
                .build();
    }

    private void read() {
        ActorRef sender = getSender();
        log.debug("Received read message from {}", sender);

        property.read().whenComplete((value, e) -> {
            if (e != null) {
                log.warning("Unable to read property: {}", e.getMessage());
            }
            else {
                try {
                    Content content = ContentManager.valueToContent(value);
                    sender.tell(new RespondRead(content), getSelf());
                }
                catch (ContentCodecException ex) {
                    log.warning("Unable to read property: {}", ex.getMessage());
                }
            }
        });
    }

    private void write(Write m) {
        ActorRef sender = getSender();
        log.debug("Received write message from {}", sender);

        try {
            Content inputContent = m.content;
            Object input = ContentManager.contentToValue(inputContent, property);

            property.write(input).whenComplete((output, e) -> {
                if (e != null) {
                    log.warning("Unable to write property: {}", e.getMessage());
                }
                else {
                    // TODO: return output if available
                    sender.tell(new Written(Content.EMPTY_CONTENT), getSelf());
                }
            });

        }
        catch (ContentCodecException e) {
            log.warning("Unable to write property: {}", e.getMessage());
        }
    }

    private void subscribe() {
        ActorRef sender = getSender();
        log.debug("Received subscribe message from {}", sender);

        property.subscribe(
                next -> {
                    try {
                        Content content = ContentManager.valueToContent(next);
                        sender.tell(new SubscriptionNext(content), getSelf());
                    }
                    catch (ContentCodecException e) {
                        // TODO: handle exception
                    }
                },
                e -> sender.tell(new SubscriptionError(e), getSelf()),
                () -> sender.tell(new SubscriptionComplete(), getSelf())
        );
    }

    public static Props props(String name, ExposedThingProperty property) {
        return Props.create(PropertyActor.class, () -> new PropertyActor(name, property));
    }
}