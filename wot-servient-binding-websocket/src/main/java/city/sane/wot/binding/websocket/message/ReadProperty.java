package city.sane.wot.binding.websocket.message;

import city.sane.wot.thing.ExposedThing;
import city.sane.wot.thing.property.ExposedThingProperty;
import org.java_websocket.WebSocket;

import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;

public class ReadProperty extends AbstractClientMessage {
    private String thingId;
    private String name;

    private ReadProperty() {
        this.thingId = null;
        this.name = null;
    }

    public ReadProperty(String thingId, String name) {
        this.thingId = Objects.requireNonNull(thingId);
        this.name = Objects.requireNonNull(name);
    }

    @Override
    public CompletableFuture<AbstractServerMessage> reply(WebSocket socket, Map<String, ExposedThing> things) {
        String id = getThingId();
        ExposedThing thing = things.get(id);

        if (thing != null) {
            String name = getName();
            ExposedThingProperty property = thing.getProperty(name);

            if (property != null) {
                return property.read().thenApply(value -> new ReadPropertyResponse(this, value));
            } else {
                // Property not found
                // FIXME: send 400er message back
                return CompletableFuture.failedFuture(null);
            }
        } else {
            // Thing not found
            // FIXME: send 400er message back
            return CompletableFuture.failedFuture(null);
        }
    }

    @Override
    public String toString() {
        return "ReadProperty [" +
                "thingId='" + thingId + '\'' +
                ", name='" + name + '\'' +
                ']';
    }

    public String getThingId() {
        return thingId;
    }

    public String getName() {
        return name;
    }
}
