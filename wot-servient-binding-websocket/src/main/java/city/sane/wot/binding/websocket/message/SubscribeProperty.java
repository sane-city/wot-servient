package city.sane.wot.binding.websocket.message;

import java.util.Objects;

public class SubscribeProperty extends AbstractMessage {
    private String thingId;
    private String name;

    public SubscribeProperty() {
        this.thingId = null;
        this.name = null;
    }

    public SubscribeProperty(String thingId, String name) {
        this.thingId = Objects.requireNonNull(thingId);
        this.name = Objects.requireNonNull(name);
    }

    public String getThingId() {
        return thingId;
    }

    public String getName() {
        return name;
    }
}
