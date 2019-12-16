package city.sane.wot.binding.websocket.message;

public class SubscribeProperty extends AbstractMessage {
    private String thingId;
    private String name;

    public SubscribeProperty() {
        this.thingId = null;
        this.name = null;
    }

    public SubscribeProperty(String thingId, String name) {
        this.thingId = thingId;
        this.name = name;
    }

    public String getThingId() {
        return thingId;
    }

    public String getName() {
        return name;
    }
}
