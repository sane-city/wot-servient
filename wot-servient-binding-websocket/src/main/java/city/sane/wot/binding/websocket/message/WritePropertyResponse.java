package city.sane.wot.binding.websocket.message;

public class WritePropertyResponse extends AbstractMessage {
    private Object value;

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
