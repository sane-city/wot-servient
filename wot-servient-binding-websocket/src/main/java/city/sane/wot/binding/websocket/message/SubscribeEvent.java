package city.sane.wot.binding.websocket.message;

import city.sane.wot.content.Content;
import city.sane.wot.content.ContentCodecException;
import city.sane.wot.content.ContentManager;
import city.sane.wot.thing.ExposedThing;
import city.sane.wot.thing.event.ExposedThingEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.function.Consumer;

public class SubscribeEvent extends ThingInteraction {
    private final Logger log = LoggerFactory.getLogger(SubscribeEvent.class);

    private SubscribeEvent() {
        super();
    }

    public SubscribeEvent(String thingId, String name) {
        super(thingId, name);
    }

    @Override
    public void reply(Consumer<AbstractServerMessage> replyConsumer,
                      Map<String, ExposedThing> things) {
        String id = getThingId();
        ExposedThing thing = things.get(id);

        if (thing != null) {
            String name = getName();
            ExposedThingEvent event = thing.getEvent(name);

            if (event != null) {
                event.subscribe(next -> {
                            log.debug("Next data received for Event '{}'", name);
                            try {
                                Content content = ContentManager.valueToContent(next);
                                replyConsumer.accept(new SubscribeNextResponse(getId(), content));
                            }
                            catch (ContentCodecException e) {
                                log.warn("Cannot process data for Event '{}': {}", name, e);
                                replyConsumer.accept(new ServerErrorResponse(this, "Cannot process data for Event: " + e.getMessage()));
                            }
                        },
                        e -> replyConsumer.accept(new ServerErrorResponse(this, "Subscription produced error: " + e.getMessage())),
                        () -> replyConsumer.accept(new SubscribeCompleteResponse(getId())));
            }
            else {
                // Event not found
                replyConsumer.accept(new ClientErrorResponse(this, "Event not found"));
            }
        }
        else {
            // Thing not found
            replyConsumer.accept(new ClientErrorResponse(this, "Thing not found"));
        }
    }

    @Override
    public String toString() {
        return "SubscribeEvent{" +
                "thingId='" + thingId + '\'' +
                ", name='" + name + '\'' +
                '}';
    }
}
