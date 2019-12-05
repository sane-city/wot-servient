package city.sane.wot.binding.websocket.message;

import city.sane.wot.content.Content;

public class WriteProperty extends AbstractMessage {

    private final String thingId;
    private final String name;
    private final Content payload;

    private WriteProperty() {
        this.thingId = null;
        this.name = null;
        this.payload = null;
    }


    public WriteProperty(String thingId, String name, Content payload) {
        this.thingId = thingId;
        this.name = name;
        this.payload = payload;
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
