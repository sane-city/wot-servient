package city.sane.wot.binding.websocket.message;

import java.util.Objects;

public class ReadPropertyResponse extends AbstractMessage {
    private Object value;

    private ReadPropertyResponse() {
        this.value = null;
    }

    public ReadPropertyResponse(Object value) {
        this.value = Objects.requireNonNull(value);
    }

    public Object getValue() {
        return value;
    }
}
