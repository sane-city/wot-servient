package city.sane.wot.binding.websocket.message;

import java.io.Writer;
import java.util.Objects;

public class WritePropertyResponse extends AbstractMessage {
    private Object value;

    private WritePropertyResponse(){
        this.value = null;
    }

    public WritePropertyResponse(Object value) {
        this.value = Objects.requireNonNull(value);
    }

    public Object getValue() {
        return value;
    }
}
