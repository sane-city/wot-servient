package city.sane.wot.binding.akka.actor;

import akka.actor.AbstractActor;
import akka.actor.Props;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import city.sane.wot.content.ContentManager;
import city.sane.wot.thing.action.ExposedThingAction;
import city.sane.wot.thing.form.Form;
import city.sane.wot.thing.form.Operation;

import static city.sane.wot.binding.akka.actor.ThingsActor.Created;

/**
 * This actor is responsible for the interaction with a {@link ExposedThingAction}.
 */
public class ActionActor extends AbstractActor {
    private final LoggingAdapter log = Logging.getLogger(getContext().getSystem(), this);
    private final String name;
    private final ExposedThingAction action;

    public ActionActor(String name, ExposedThingAction action) {
        this.name = name;
        this.action = action;
    }

    @Override
    public void preStart() {
        log.info("Started");

        String href = getSelf().path().toStringWithAddress(getContext().getSystem().provider().getDefaultAddress());
        Form form = new Form.Builder()
                .setHref(href)
                .setContentType(ContentManager.DEFAULT)
                .setOp(Operation.INVOKE_ACTION)
                .build();

        action.addForm(form);
        log.info("Assign '{}' to Action '{}'", href, name);

        getContext().getParent().tell(new Created<>(getSelf()), getSelf());
    }

    @Override
    public void postStop() {
        log.info("Stopped");
    }

    @Override
    public AbstractActor.Receive createReceive() {
        // FIXME: Add support for invoking this action
        return receiveBuilder()
                .build();
    }

    public static Props props(String name, ExposedThingAction action) {
        return Props.create(ActionActor.class, () -> new ActionActor(name, action));
    }
}