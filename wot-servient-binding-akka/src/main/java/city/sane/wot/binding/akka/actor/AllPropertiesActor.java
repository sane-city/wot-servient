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
import city.sane.wot.thing.ExposedThing;
import city.sane.wot.thing.form.Form;
import city.sane.wot.thing.form.Operation;

import static city.sane.wot.binding.akka.Messages.RespondRead;

/**
 * This Actor is responsible for reading all {@link city.sane.wot.thing.property.ExposedThingProperty} at the same time.
 */
class AllPropertiesActor extends AbstractActor {
    private final LoggingAdapter log = Logging.getLogger(getContext().getSystem(), this);
    private final ExposedThing thing;

    private AllPropertiesActor(ExposedThing thing) {
        this.thing = thing;
    }

    @Override
    public void preStart() {
        log.info("Started");

        String href = MediatorActor.remoteOverlayPath(getSelf().path()).toString();
        Form form = new Form.Builder()
                .setHref(href)
                .setContentType(ContentManager.DEFAULT)
                .setOp(Operation.READ_ALL_PROPERTIES, Operation.READ_MULTIPLE_PROPERTIES/*, Operation.writeallproperties, Operation.writemultipleproperties*/)
                .build();

        thing.addForm(form);
        log.info("Assign '{}' for reading all properties", href);

        getContext().getParent().tell(new ThingsActor.Created<>(getSelf()), getSelf());
    }

    @Override
    public void postStop() {
        log.info("Stopped");
    }

    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .match(city.sane.wot.binding.akka.Messages.Read.class, m -> read())
                .build();
    }

    private void read() {
        ActorRef sender = getSender();

        thing.readProperties().whenComplete((value, e) -> {
            if (e != null) {
                log.warning("Unable to read properties: {}", e.getMessage());
            }

            try {
                Content content = ContentManager.valueToContent(value);
                sender.tell(new RespondRead(content), getSelf());
            }
            catch (ContentCodecException ex) {
                log.warning("Unable to read property: {}", ex.getMessage());
            }
        });
    }

    public static Props props(ExposedThing thing) {
        return Props.create(AllPropertiesActor.class, () -> new AllPropertiesActor(thing));
    }
}
