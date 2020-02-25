package city.sane.wot.binding.websocket.message;

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
    public void reply(Consumer<AbstractServerMessage> replyConsumer,
                      Map<String, ExposedThing> things) {
        String id = getThingId();
        ExposedThing thing = things.get(id);

        if (thing != null) {
            String name = getName();
            ExposedThingProperty<Object> property = thing.getProperty(name);

            if (property != null) {
                property.observer()
                        .map(optional -> ContentManager.valueToContent(optional.orElse(null)))
                        .subscribe(
                                content -> {
                                    log.debug("Next data received for Property '{}'", name);
                                    replyConsumer.accept(new SubscribeNextResponse(getId(), content));
                                },
                                e -> {
                                    log.warn("Cannot process data for Property '{}': {}", name, e);
                                    replyConsumer.accept(new ServerErrorResponse(this, "Subscription produced error: " + e.getMessage()));
                                },
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
