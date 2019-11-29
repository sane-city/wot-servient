package city.sane.wot.binding.websocket.message;

public class WriteProperty extends AbstractMessage {

    private final String thingId;
    private final String name;
    private final Object value;


    public WriteProperty(String thingId, String name, Object value) {
        this.thingId = thingId;
        this.name = name;
        this.value = value;
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

    public Object getValue() {
        return value;
    }
}
