package city.sane.wot.binding.akka.actor;

import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.actor.Props;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import city.sane.wot.content.Content;
import city.sane.wot.content.ContentCodecException;
import city.sane.wot.content.ContentManager;
import city.sane.wot.thing.action.ExposedThingAction;
import city.sane.wot.thing.form.Form;
import city.sane.wot.thing.form.Operation;

import java.io.Serializable;

import static city.sane.wot.binding.akka.actor.ThingsActor.Created;

/**
 * This actor is responsible for the interaction with a {@link ExposedThingAction}.
 */
public class ActionActor extends AbstractActor {
    private final LoggingAdapter log = Logging.getLogger(getContext().getSystem(), this);
    private final String name;
    private final ExposedThingAction action;

    private ActionActor(String name, ExposedThingAction action) {
        this.name = name;
        this.action = action;
    }

    @Override
    public void preStart() {
        log.info("Started");

        String href = getSelf().path().toString().replaceAll("akka:", "bud:");
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

    public static Props props(String name, ExposedThingAction action) {
        return Props.create(ActionActor.class, () -> new ActionActor(name, action));
    }

    public static class Invoke implements Serializable {
        private final Content content;

        public Invoke(Content content) {
            this.content = content;
        }
    }

    public static class Invoked implements Serializable {
        public final Content content;

        public Invoked(Content content) {
            this.content = content;
        }
    }
}
