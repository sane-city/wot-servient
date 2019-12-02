package city.sane.wot.binding.websocket.message;

public class WritePropertyResponse extends AbstractMessage {
    private Object value;

    public WritePropertyResponse(Object value) {
        this.value = value;
    }

    @Override
    public String getType() {
        return null;
    }

    public Object getValue() {
        return value;
    }
}
