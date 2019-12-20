package city.sane.wot.binding.websocket.message;

import city.sane.wot.content.Content;
import city.sane.wot.content.ContentCodecException;
import city.sane.wot.content.ContentManager;
import city.sane.wot.thing.ExposedThing;
import city.sane.wot.thing.property.ExposedThingProperty;
import org.java_websocket.WebSocket;

import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;

public class WriteProperty extends ThingInteractionWithContent {
    private WriteProperty() {
        super();
    }

    public WriteProperty(String thingId, String name, Content value) {
        super(thingId, name, value);
    }

    @Override
    public CompletableFuture<AbstractServerMessage> reply(WebSocket socket, Map<String, ExposedThing> things) {
        String id = getThingId();
        ExposedThing thing = things.get(id);

        if (thing != null) {
            String name = getName();
            ExposedThingProperty property = thing.getProperty(name);

            if (property != null) {
                Content payload = getValue();

                try {
                    Object input = ContentManager.contentToValue(payload, property);

                    return property.write(input).thenApply(output -> {
                        try {
                            return new WritePropertyResponse(getId(), ContentManager.valueToContent(output));
                        }
                        catch (ContentCodecException e) {
                            throw new CompletionException(e);
                        }
                    });
                } catch (ContentCodecException e) {
                    // unable to parse paylod
                    // FIXME: send 500er error back and remove throw
                    return CompletableFuture.completedFuture(new ServerErrorResponse(this,"500 Internal Server Error"));
                }
            } else {
                // Property not found
                return CompletableFuture.completedFuture(new ClientErrorResponse(this,"404 Property not found"));
            }
        } else {
            // Thing not found
            return CompletableFuture.completedFuture(new ClientErrorResponse(this,"404 Thing not found"));
        }
    }

    @Override
    public String toString() {
        return "WriteProperty [" +
                "thingId='" + thingId + '\'' +
                ", name='" + name + '\'' +
                ", value=" + value +
                ']';
    }
}
