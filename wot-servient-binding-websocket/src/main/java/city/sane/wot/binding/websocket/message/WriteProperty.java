package city.sane.wot.binding.websocket.message;

public class WriteProperty extends AbstractMessage {

    private final String thingId;
    private final String name;
    private final byte[] payload;

    public WriteProperty() {
        this.thingId = null;
        this.name = null;
        this.payload = null;
    }


    public WriteProperty(String thingId, String name, byte[] payload) {
        this.thingId = thingId;
        this.name = name;
        this.payload = payload;
    }

    @Override
    public String getType() {
        return "writeProperty";
    }

    public String getThingId() {
        return thingId;
    }

    public String getName() {
        return name;
    }

    public byte[] getPayload() {
        return payload;
    }
}
