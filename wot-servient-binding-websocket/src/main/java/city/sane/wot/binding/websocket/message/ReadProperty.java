package city.sane.wot.binding.websocket.message;

import city.sane.wot.content.ContentCodecException;
import city.sane.wot.content.ContentManager;
import city.sane.wot.thing.ExposedThing;
import city.sane.wot.thing.property.ExposedThingProperty;

import java.util.Map;
import java.util.function.Consumer;

public class ReadProperty extends ThingInteraction {
    private ReadProperty() {
        super();
    }

    public ReadProperty(String thingId, String name) {
        super(thingId, name);
    }

    @Override
    public void reply(Consumer<AbstractServerMessage> replyConsumer,
                      Map<String, ExposedThing> things) {
        String id = getThingId();
        ExposedThing thing = things.get(id);

        if (thing != null) {
            String name = getName();
            ExposedThingProperty property = thing.getProperty(name);

            if (property != null) {
                property.read().thenAccept(value -> {
                    try {
                        replyConsumer.accept(new ReadPropertyResponse(this, ContentManager.valueToContent(value)));
                    }
                    catch (ContentCodecException e) {
                        replyConsumer.accept(new ServerErrorResponse(this, "Unable to parse output of write operation: " + e.getMessage()));
                    }
                });
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
        return "ReadProperty{" +
                "thingId='" + thingId + '\'' +
                ", name='" + name + '\'' +
                '}';
    }
}
