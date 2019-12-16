package city.sane.wot.binding.websocket.message;

import java.util.Objects;

public class SubscribePropertyResponse extends AbstractMessage {


    private Object value;

    private SubscribePropertyResponse() {
        this.value = null;
    }

    public SubscribePropertyResponse(Object value) {
        this.value = Objects.requireNonNull(value);
    }


    public Object getValue() {
        return value;
    }


}
