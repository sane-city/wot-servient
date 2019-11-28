package city.sane.wot.binding.websocket.message;

public class WriteProperty extends AbstractMessage {

    private final String thingId;
    private final String name;

    public WriteProperty(String thingId, String name) {
        this.thingId = thingId;
        this.name = name;
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
}
