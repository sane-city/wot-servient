package city.sane.wot.binding.websocket.message;

import city.sane.wot.content.Content;
import city.sane.wot.content.ContentCodecException;
import city.sane.wot.content.ContentManager;
import city.sane.wot.thing.ExposedThing;
import city.sane.wot.thing.property.ExposedThingProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.java_websocket.WebSocket;

import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;

public class WriteProperty extends AbstractClientMessage {
    private String thingId;
    private String name;
    private Content payload;

    private WriteProperty() {
        this.thingId = null;
        this.name = null;
        this.payload = null;
    }

    @Override
    public CompletableFuture<AbstractServerMessage> reply(WebSocket socket, Map<String, ExposedThing> things) {
        String id = getThingId();
        ExposedThing thing = things.get(id);

        if (thing != null) {
            String name = getName();
            ExposedThingProperty property = thing.getProperty(name);

            if (property != null) {
                Content payload = getPayload();

                try {
                    Object input = ContentManager.contentToValue(payload, property);

                    return property.write(input).thenApply(output -> new WritePropertyResponse(this, output));
                }
                catch (ContentCodecException e) {
                    // unable to parse paylod
                    // FIXME: send 500er error back and remove throw
                    return CompletableFuture.failedFuture(null);
                }
            }
            else {
                // Property not found
                // FIXME: send 400er message back
                return CompletableFuture.failedFuture(null);
            }
        }
        else {
            // Thing not found
            // FIXME: send 400er message back
            return CompletableFuture.failedFuture(null);
        }
    }

    public WriteProperty(String thingId, String name, Content payload) {
        this.thingId = Objects.requireNonNull(thingId);
        this.name = Objects.requireNonNull(name);
        this.payload = Objects.requireNonNull(payload);
    }

    public String getThingId() {
        return thingId;
    }

    public String getName() {
        return name;
    }

    public Content getPayload() {
        return payload;
    }

    @Override
    public String toString() {
        return "WriteProperty [" +
                "thingId='" + thingId + '\'' +
                ", name='" + name + '\'' +
                ", payload=" + payload +
                ']';
    }
}
