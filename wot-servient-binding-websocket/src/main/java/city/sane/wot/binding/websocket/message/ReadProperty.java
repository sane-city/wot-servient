package city.sane.wot.binding.websocket.message;

import city.sane.wot.thing.ExposedThing;

import java.util.Map;

public class ReadProperty extends AbstractMessage {
    private final Map<String, ExposedThing> things;

    public ReadProperty(Map<String, ExposedThing> things) {
        this.things = things;
    }

    @Override
    public String getType() {
        return "readProperty";
    }
}
