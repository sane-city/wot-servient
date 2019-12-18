package city.sane.wot.binding.websocket.message;

import city.sane.wot.content.Content;

public class WritePropertyResponse extends AbstractServerMessage {
    private final Content value;

    private WritePropertyResponse() {
        super();
        value = null;
    }

    public WritePropertyResponse(String id, Content value) {
        super(id);
        this.value = value;
    }

    public Content getValue() {
        return value;
    }

    @Override
    public String toString() {
        return "WritePropertyResponse [" +
                "value=" + value +
                ", id='" + id + '\'' +
                ']';
    }
}
