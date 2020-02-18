package city.sane.wot.binding.akka.actor;

import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.actor.Props;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import city.sane.wot.binding.akka.Messages.Invoke;
import city.sane.wot.binding.akka.Messages.Invoked;
import city.sane.wot.content.Content;
import city.sane.wot.content.ContentCodecException;
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
    private final ExposedThingAction<Object, Object> action;

    private ActionActor(String name, ExposedThingAction<Object, Object> action) {
        this.name = name;
        this.action = action;
    }

    @Override
    public void preStart() {
        log.debug("Started");

        String href = getSelf().path().toStringWithAddress(getContext().getSystem().provider().getDefaultAddress());
        Form form = new Form.Builder()
                .setHref(href)
                .setContentType(ContentManager.DEFAULT)
                .setOp(Operation.INVOKE_ACTION)
                .build();

        action.addForm(form);
        log.debug("Assign '{}' to Action '{}'", href, name);

        getContext().getParent().tell(new Created<>(getSelf()), getSelf());
    }

    @Override
    public void postStop() {
        log.debug("Stopped");
    }

    @Override
    public AbstractActor.Receive createReceive() {
        // FIXME: Add support for invoking this action
        return receiveBuilder()
                .match(Invoke.class, this::read)
                .build();
    }

    private void read(Invoke m) {
        try {
            ActorRef sender = getSender();
            Content inputContent = m.content;
            Object input = ContentManager.contentToValue(inputContent, action.getInput());

            action.invoke(input).whenComplete((output, e) -> {
                if (e != null) {
                    log.warning("Unable to write property: {}", e.getMessage());
                }
                else {
                    try {
                        Content outputContent = ContentManager.valueToContent(output);
                        sender.tell(new Invoked(outputContent), getSelf());
                    }
                    catch (ContentCodecException ex) {
                        log.warning("Unable to parse output: {}", e.getMessage());
                    }
                }
            });
        }
        catch (ContentCodecException e) {
            log.warning("Unable to write property: {}", e.getMessage());
        }
    }

    public static Props props(String name, ExposedThingAction<Object, Object> action) {
        return Props.create(ActionActor.class, () -> new ActionActor(name, action));
    }
}
