package city.sane.wot.binding.websocket.message;

import city.sane.wot.content.Content;
import city.sane.wot.content.ContentCodecException;
import city.sane.wot.content.ContentManager;
import city.sane.wot.thing.ExposedThing;
import city.sane.wot.thing.property.ExposedThingProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.function.Consumer;

public class SubscribeProperty extends ThingInteraction {
    private final Logger log = LoggerFactory.getLogger(SubscribeProperty.class);

    private SubscribeProperty() {
        super();
    }

    public SubscribeProperty(String thingId, String name) {
        super(thingId, name);
    }

    @Override
    public void reply(Consumer<AbstractServerMessage> replyConsumer, Map<String, ExposedThing> things) {
        String id = getThingId();
        ExposedThing thing = things.get(id);

        if (thing != null) {
            String name = getName();
            ExposedThingProperty property = thing.getProperty(name);

            if (property != null) {
                property.subscribe(next -> {
                            log.debug("Next data received for Property '{}'", name);
                            try {
                                Content content = ContentManager.valueToContent(next);
                                replyConsumer.accept(new SubscribeNextResponse(getId(), content));
                            }
                            catch (ContentCodecException e) {
                                log.warn("Cannot process data for Property '{}': {}", name, e);
                                replyConsumer.accept(new ServerErrorResponse(this, "Cannot process data for Property: " + e.getMessage()));
                            }
                        },
                        e -> replyConsumer.accept(new ServerErrorResponse(this, "Subscription produced error: " + e.getMessage())),
                        () -> replyConsumer.accept(new SubscribeCompleteResponse(getId())));
            }
            else {
                // Property not found
                replyConsumer.accept(new ClientErrorResponse(this, "Property not found"));
            }
        }
        else {
            // Thing not found
            replyConsumer.accept(new ClientErrorResponse(this, "Thing not found"));
        }
    }

    @Override
    public String toString() {
        return "SubscribeProperty{" +
                "thingId='" + thingId + '\'' +
                ", name='" + name + '\'' +
                '}';
    }
}
