package city.sane.wot.binding.akka.actor;

import akka.actor.AbstractActor;
import akka.actor.Props;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import city.sane.wot.thing.content.ContentManager;
import city.sane.wot.thing.event.ExposedThingEvent;
import city.sane.wot.thing.form.Form;
import city.sane.wot.thing.form.Operation;

import static city.sane.wot.binding.akka.CrudMessages.Created;

/**
 * This actor is responsible for the interaction with a {@link ExposedThingEvent}.
 */
public class EventActor extends AbstractActor {
    private final LoggingAdapter log = Logging.getLogger(getContext().getSystem(), this);
    private final String name;
    private final ExposedThingEvent event;

    public EventActor(String name, ExposedThingEvent event) {
        this.name = name;
        this.event = event;
    }

    @Override
    public void preStart() {
        log.info("Started");

        String href = getSelf().path().toStringWithAddress(getContext().getSystem().provider().getDefaultAddress());
        Form form = new Form.Builder()
                .setHref(href)
                .setContentType(ContentManager.DEFAULT)
                .setSubprotocol("longpoll")
                .setOp(Operation.subscribeevent)
                .build();

        event.addForm(form);
        log.info("Assign '{}' to Event '{}'", href, name);

        getContext().getParent().tell(new Created<>(getSelf()), getSelf());
    }

    @Override
    public void postStop() {
        log.info("Stopped");
    }

    @Override
    public AbstractActor.Receive createReceive() {
        return receiveBuilder()
                .build();
    }

    static public Props props(String name, ExposedThingEvent event) {
        return Props.create(EventActor.class, () -> new EventActor(name, event));
    }
}