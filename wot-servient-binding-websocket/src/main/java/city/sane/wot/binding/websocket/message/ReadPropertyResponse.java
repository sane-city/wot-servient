package city.sane.wot.binding.websocket.message;

import java.util.Objects;

public class ReadPropertyResponse extends AbstractServerMessage {
    private Object value;

    private ReadPropertyResponse() {
        super();
        this.value = null;
    }

    public ReadPropertyResponse(Object value) {
        super();
        this.value = Objects.requireNonNull(value);
    }

    public ReadPropertyResponse(ReadProperty clientMessage, Object value) {
        super(clientMessage);
        this.value = value;
    }

    public Object getValue() {
        return value;
    }
}
