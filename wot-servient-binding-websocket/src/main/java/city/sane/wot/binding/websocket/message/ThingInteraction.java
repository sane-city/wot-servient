package city.sane.wot.binding.websocket.message;

import java.util.Objects;

public abstract class ThingInteraction extends AbstractClientMessage {
    protected final String thingId;
    protected final String name;

    protected ThingInteraction() {
        this.thingId = null;
        this.name = null;
    }

    public ThingInteraction(String thingId, String name) {
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
