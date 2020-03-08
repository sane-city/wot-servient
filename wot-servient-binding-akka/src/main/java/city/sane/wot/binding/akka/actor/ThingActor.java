package city.sane.wot.binding.akka.actor;

import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.actor.Props;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import city.sane.wot.binding.akka.Message;
import city.sane.wot.binding.akka.Message.*;
import city.sane.wot.binding.akka.actor.ThingsActor.Destroyed;
import city.sane.wot.binding.akka.actor.ThingsActor.Exposed;
import city.sane.wot.content.Content;
import city.sane.wot.content.ContentCodecException;
import city.sane.wot.content.ContentManager;
import city.sane.wot.thing.ExposedThing;
import city.sane.wot.thing.action.ExposedThingAction;
import city.sane.wot.thing.event.ExposedThingEvent;
import city.sane.wot.thing.form.Form;
import city.sane.wot.thing.form.Operation;
import city.sane.wot.thing.property.ExposedThingProperty;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.Objects;

import static java.util.Objects.requireNonNull;

/**
 * This Actor is responsible for the interaction with the respective Thing. It is started as soon as
 * a thing is to be exposed and terminated when the thing should no longer be exposed.<br> For this
 * purpose, the actuator creates a series of child actuators that allow interaction with a single
 * {@link city.sane.wot.thing.property.ExposedThingProperty}, {@link
 * city.sane.wot.thing.action.ExposedThingAction}, or {@link city.sane.wot.thing.event.ExposedThingEvent}.
 */
public class ThingActor extends AbstractActor {
    private final LoggingAdapter log = Logging.getLogger(getContext().getSystem(), this);
    private final ExposedThing thing;

    private ThingActor(ExposedThing thing) {
        this.thing = thing;
    }

    @Override
    public void preStart() {
        log.debug("Started");

        String href = getSelf().path().toStringWithAddress(getContext().getSystem().provider().getDefaultAddress());

        // all properties
        String allPropertiesHref = href + "#all/properties";
        Form form = new Form.Builder()
                .setHref(allPropertiesHref)
                .setContentType(ContentManager.DEFAULT)
                .setOp(Operation.READ_ALL_PROPERTIES, Operation.READ_MULTIPLE_PROPERTIES/*, Operation.writeallproperties, Operation.writemultipleproperties*/)
                .build();

        thing.addForm(form);
        log.debug("Assign '{}' for reading all properties", allPropertiesHref);

        // properties
        thing.getProperties().forEach((name, property) -> {
            String propertyHref = href + "#properties/" + name;
            Form.Builder builder = new Form.Builder()
                    .setHref(propertyHref)
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
            log.debug("Assign '{}' to Property '{}'", propertyHref, name);
        });

        // actions
        thing.getActions().forEach((name, action) -> {
            String actionHref = href + "#actions/" + name;
            Form actionForm = new Form.Builder()
                    .setHref(actionHref)
                    .setContentType(ContentManager.DEFAULT)
                    .setOp(Operation.INVOKE_ACTION)
                    .build();

            action.addForm(actionForm);
            log.debug("Assign '{}' to Action '{}'", actionHref, name);
        });

        // events
        thing.getEvents().forEach((name, event) -> {
            String eventHref = href + "#events/" + name;
            Form eventForm = new Form.Builder()
                    .setHref(eventHref)
                    .setContentType(ContentManager.DEFAULT)
                    .setSubprotocol("longpoll")
                    .setOp(Operation.SUBSCRIBE_EVENT)
                    .build();

            event.addForm(eventForm);
            log.debug("Assign '{}' to Event '{}'", eventHref, name);
        });

        log.debug("Thing has been exposed");
        getContext().getParent().tell(new Exposed(thing.getId()), getSelf());
    }

    @Override
    public void postStop() {
        log.debug("Stopped");
        getContext().getParent().tell(new Destroyed(thing.getId()), getSelf());
    }

    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .match(GetThingDescription.class, m -> getThingDescription())
                .match(ReadAllProperties.class, m -> readAllProperties())
                .match(ReadProperty.class, this::readProperty)
                .match(WriteProperty.class, this::writeProperty)
                .match(SubscribeProperty.class, this::subscribeProperty)
                .match(SubscribeEvent.class, this::subscribeEvent)
                .match(InvokeAction.class, this::invokeAction)
                .build();
    }

    private void getThingDescription() {
        ActorRef sender = getSender();
        log.debug("Received ThingDescription message from {}", sender);

        try {
            Content content = ContentManager.valueToContent(thing);
            sender.tell(new ThingDescription(content), getSelf());
        }
        catch (ContentCodecException e) {
            log.warning("Unable to get thing description: {}", e.getMessage());
            sender.tell(new GetThingDescriptionFailed(e), getSelf());
        }
    }

    private void readAllProperties() {
        ActorRef sender = getSender();
        log.debug("Received ReadAllProperties message from {}", sender);

        thing.readProperties().thenAccept(values -> {
            try {
                Content content = ContentManager.valueToContent(values);
                sender.tell(new PropertiesValues(content), getSelf());
            }
            catch (ContentCodecException e) {
                log.warning("Unable to read all properties: {}", e.getMessage());
                sender.tell(new ReadAllPropertiesFailed(e), getSelf());
            }
        });
    }

    private void readProperty(ReadProperty m) {
        ActorRef sender = getSender();
        log.debug("Received ReadProperty message from {}", sender);

        ExposedThingProperty<Object> property = thing.getProperty(m.name);
        if (property != null) {
            property.read().whenComplete((value, e) -> {
                if (e != null) {
                    log.warning("Unable to read property: {}", e.getMessage());
                }
                else {
                    try {
                        Content content = ContentManager.valueToContent(value);
                        sender.tell(new ReadPropertyResponse(content), getSelf());
                    }
                    catch (ContentCodecException ex) {
                        log.warning("Unable to read property: {}", ex.getMessage());
                        sender.tell(new ReadPropertyFailed(e), getSelf());
                    }
                }
            });
        }
        else {
            log.warning("Property with name {} not found", m.name);
            sender.tell(new ReadPropertyFailed(new Exception("Property with name " + m.name + " not found")), getSelf());
        }
    }

    private void writeProperty(WriteProperty m) {
        ActorRef sender = getSender();
        log.debug("Received WriteProperty message from {}", sender);

        ExposedThingProperty<Object> property = thing.getProperty(m.name);
        if (property != null) {
            try {
                Content inputContent = m.content;
                Object input = ContentManager.contentToValue(inputContent, property);

                property.write(input).whenComplete((output, e) -> {
                    if (e != null) {
                        log.warning("Unable to write property: {}", e.getMessage());
                    }
                    else {
                        if (output != null) {
                            try {
                                Content content = ContentManager.valueToContent(output);
                                sender.tell(new WrittenProperty(content), getSelf());
                            }
                            catch (ContentCodecException ex) {
                                log.warning("Unable to parse response content of write property operation. Return empty content: {}", ex.getMessage());
                                sender.tell(new WritePropertyFailed(ex), getSelf());
                            }
                        }
                        else {
                            sender.tell(new WrittenProperty(Content.EMPTY_CONTENT), getSelf());
                        }
                    }
                });
            }
            catch (ContentCodecException e) {
                log.warning("Unable to write property: {}", e.getMessage());
                sender.tell(new WritePropertyFailed(e), getSelf());
            }
        }
        else {
            log.warning("Property with name {} not found", m.name);
            sender.tell(new WritePropertyFailed(new Exception("Property with name " + m.name + " not found")), getSelf());
        }
    }

    private void subscribeProperty(SubscribeProperty m) {
        ActorRef sender = getSender();
        log.debug("Received SubscribeProperty message from {}", sender);

        ExposedThingProperty<Object> property = thing.getProperty(m.name);
        if (property != null) {
            property.observer()
                    .map(optional -> ContentManager.valueToContent(optional.orElse(null)))
                    .subscribe(
                            content -> sender.tell(new Message.SubscriptionNext(content), getSelf()),
                            e -> sender.tell(new SubscriptionError(e), getSelf()),
                            () -> sender.tell(new Message.SubscriptionComplete(), getSelf())
                    );
        }
        else {
            log.warning("Property with name {} not found", m.name);
            sender.tell(new SubscribeFailed(new Exception("Property with name " + m.name + " not found")), getSelf());
        }
    }

    private void subscribeEvent(SubscribeEvent m) {
        ActorRef sender = getSender();
        log.debug("Received SubscribeEvent message from {}", sender);

        ExposedThingEvent<Object> event = thing.getEvent(m.name);
        if (event != null) {
            event.observer()
                    .map(optional -> ContentManager.valueToContent(optional.orElse(null)))
                    .subscribe(
                            content -> sender.tell(new Message.SubscriptionNext(content), getSelf()),
                            e -> sender.tell(new SubscriptionError(e), getSelf()),
                            () -> sender.tell(new Message.SubscriptionComplete(), getSelf())
                    );
        }
        else {
            log.warning("Event with name {} not found", m.name);
            sender.tell(new SubscribeFailed(new Exception("Event with name " + m.name + " not found")), getSelf());
        }
    }

    private void invokeAction(InvokeAction m) {
        ActorRef sender = getSender();
        log.debug("Received InvokeAction message from {}", sender);

        ExposedThingAction<Object, Object> action = thing.getAction(m.name);
        if (action != null) {
            try {
                Content inputContent = m.content;
                Object input = ContentManager.contentToValue(inputContent, action.getInput());

                action.invoke(input).whenComplete((output, e) -> {
                    if (e != null) {
                        log.warning("Unable to write property: {}", e.getMessage());
                        sender.tell(new InvokeActionFailed(e), getSelf());
                    }
                    else {
                        try {
                            Content outputContent = ContentManager.valueToContent(output);
                            sender.tell(new InvokedAction(outputContent), getSelf());
                        }
                        catch (ContentCodecException ex) {
                            log.warning("Unable to parse output: {}", e.getMessage());
                            sender.tell(new InvokeActionFailed(e), getSelf());
                        }
                    }
                });
            }
            catch (ContentCodecException e) {
                log.warning("Unable to write property: {}", e.getMessage());
                sender.tell(new InvokeActionFailed(e), getSelf());
            }
        }
        else {
            log.warning("Action with name {} not found", m.name);
            sender.tell(new InvokeActionFailed(new Exception("Action with name " + m.name + " not found")), getSelf());
        }
    }

    public static Props props(ExposedThing thing) {
        requireNonNull(thing);
        return Props.create(ThingActor.class, () -> new ThingActor(thing));
    }

    // https://stackoverflow.com/a/53845446/1074188
    @JsonIgnoreProperties({ "hibernateLazyInitializer", "handler" })
    public static class GetThingDescription implements Message {
        public GetThingDescription() {
            // required by jackson
        }

        @Override
        public String toString() {
            return "GetThingDescription{}";
        }
    }

    public static class ThingDescription extends ContentMessage {
        public ThingDescription(Content content) {
            super(content);
        }

        @Override
        public int hashCode() {
            return Objects.hash(content);
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }
            ThingDescription that = (ThingDescription) o;
            return Objects.equals(content, that.content);
        }
    }

    public static class GetThingDescriptionFailed extends ErrorMessage {
        protected GetThingDescriptionFailed(Throwable e) {
            super(e);
        }
    }

    // https://stackoverflow.com/a/53845446/1074188
    @JsonIgnoreProperties({ "hibernateLazyInitializer", "handler" })
    public static class ReadAllProperties implements Message {
        public ReadAllProperties() {
            // required by jackson
        }

        @Override
        public String toString() {
            return "ReadAllProperties{}";
        }
    }

    public static class ReadAllPropertiesFailed extends ErrorMessage {
        protected ReadAllPropertiesFailed(Throwable e) {
            super(e);
        }
    }

    public static class PropertiesValues extends ContentMessage {
        public PropertiesValues(Content content) {
            super(content);
        }
    }

    public static class ReadProperty extends InteractionMessage {
        public ReadProperty(String name) {
            super(name);
        }
    }

    public static class ReadPropertyResponse extends ContentMessage {
        public ReadPropertyResponse(Content content) {
            super(content);
        }
    }

    public static class ReadPropertyFailed extends ErrorMessage {
        protected ReadPropertyFailed(Throwable e) {
            super(e);
        }
    }

    public static class WriteProperty extends InteractionWithContentMessage {
        public WriteProperty(String name, Content content) {
            super(name, content);
        }
    }

    public static class WrittenProperty extends ContentMessage {
        public WrittenProperty(Content content) {
            super(content);
        }
    }

    public static class WritePropertyFailed extends ErrorMessage {
        protected WritePropertyFailed(Throwable e) {
            super(e);
        }
    }

    public static class SubscribeProperty extends InteractionMessage {
        public SubscribeProperty(String name) {
            super(name);
        }
    }

    public static class SubscribeEvent extends InteractionMessage {
        public SubscribeEvent(String name) {
            super(name);
        }
    }

    public static class InvokeAction extends InteractionWithContentMessage {
        public InvokeAction(String name) {
            super(name);
        }

        public InvokeAction(String name, Content content) {
            super(name, content);
        }
    }

    public static class InvokedAction extends ContentMessage {
        public InvokedAction(Content content) {
            super(content);
        }
    }

    public static class InvokeActionFailed extends ErrorMessage {
        protected InvokeActionFailed(Throwable e) {
            super(e);
        }
    }
}
