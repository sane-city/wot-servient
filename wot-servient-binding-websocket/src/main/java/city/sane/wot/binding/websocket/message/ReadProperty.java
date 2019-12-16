package city.sane.wot.binding.websocket.message;

import java.io.Reader;
import java.util.Objects;

public class ReadProperty extends AbstractMessage {
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


    public String getThingId() {
        return thingId;
    }

    public String getName() {
        return name;
    }
}
