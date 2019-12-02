package city.sane.wot.binding.websocket.message;

import java.io.Writer;

public class WritePropertyResponse extends AbstractMessage {
    private Object value;

    private WritePropertyResponse(){
        this.value = null;
    }

    public WritePropertyResponse(Object value) {
        this.value = value;
    }

    @Override
    public String getType() {
        return "writePropertyResponse";
    }

    public Object getValue() {
        return value;
    }
}
