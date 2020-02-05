package city.sane.wot.binding.websocket.message;

import city.sane.wot.content.Content;
import city.sane.wot.content.ContentCodecException;
import city.sane.wot.content.ContentManager;
import city.sane.wot.thing.ExposedThing;
import city.sane.wot.thing.property.ExposedThingProperty;

import java.util.Map;
import java.util.function.Consumer;

public class WriteProperty extends ThingInteractionWithContent {
    private WriteProperty() {
        super();
    }

    public WriteProperty(String thingId, String name, Content value) {
        super(thingId, name, value);
    }

    @Override
    public void reply(Consumer<AbstractServerMessage> replyConsumer, Map<String, ExposedThing> things) {
        String id = getThingId();
        ExposedThing thing = things.get(id);

        if (thing != null) {
            String name = getName();
            ExposedThingProperty property = thing.getProperty(name);

            if (property != null) {
                Content payload = getValue();

                try {
                    Object input = ContentManager.contentToValue(payload, property);

                    property.write(input).thenAccept(output -> {
                        try {
                            replyConsumer.accept(new WritePropertyResponse(getId(), ContentManager.valueToContent(output)));
                        }
                        catch (ContentCodecException e) {
                            replyConsumer.accept(new ServerErrorResponse(this,"Unable to parse output of write operation: " + e.getMessage()));
                        }
                    });
                } catch (ContentCodecException e) {
                    // unable to parse paylod
                    replyConsumer.accept(new ServerErrorResponse(this, "Unable to parse given input " + e.getMessage()));
                }
            } else {
                // Property not found
                replyConsumer.accept(new ClientErrorResponse(this, "Property not found"));
            }
        } else {
            // Thing not found
            replyConsumer.accept(new ClientErrorResponse(this, "Thing not found"));
        }
    }

    @Override
    public String toString() {
        return "WriteProperty{" +
                "value=" + value +
                ", thingId='" + thingId + '\'' +
                ", name='" + name + '\'' +
                '}';
    }
}
