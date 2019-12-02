package city.sane.wot.binding.websocket.message;

import java.io.Reader;

public class ReadProperty extends AbstractMessage {
    private final String thingId;
    private final String name;

    private ReadProperty() {
        this.thingId = null;
        this.name = null;
    }

    public ReadProperty(String thingId, String name) {
        this.thingId = thingId;
        this.name = name;
    }

    @Override
    public String getType() {
        return "readProperty";
    }

    public String getThingId() {
        return thingId;
    }

    public String getName() {
        return name;
    }
}
