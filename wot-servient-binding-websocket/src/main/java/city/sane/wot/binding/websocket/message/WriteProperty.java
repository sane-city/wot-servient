package city.sane.wot.binding.websocket.message;

import city.sane.wot.content.Content;

import java.util.Objects;

public class WriteProperty extends AbstractMessage {

    private String thingId;
    private String name;
    private Content payload;

    private WriteProperty() {
        this.thingId = null;
        this.name = null;
        this.payload = null;
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
}
