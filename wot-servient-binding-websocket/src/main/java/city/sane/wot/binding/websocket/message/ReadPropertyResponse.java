package city.sane.wot.binding.websocket.message;

public class ReadPropertyResponse extends AbstractMessage {
    private final Object value;

    private ReadPropertyResponse() {
        this.value = null;
    }

    public ReadPropertyResponse(Object value) {
        this.value = value;
    }

    @Override
    public String getType() {
        return "readPropertyResponse";
    }

    public Object getValue() {
        return value;
    }
}
