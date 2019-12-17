package city.sane.wot.binding.websocket.message;

import java.io.Writer;
import java.util.Objects;

public class WritePropertyResponse extends AbstractServerMessage {
    private Object value;

    private WritePropertyResponse(){
        value = null;
    }

    public WritePropertyResponse(AbstractClientMessage clientMessage, Object value) {
        super(clientMessage);
        this.value = value;
    }

    public Object getValue() {
        return value;
    }

    @Override
    public String toString() {
        return "WritePropertyResponse [" +
                "value=" + value +
                ", clientId='" + clientId + '\'' +
                ']';
    }
}
