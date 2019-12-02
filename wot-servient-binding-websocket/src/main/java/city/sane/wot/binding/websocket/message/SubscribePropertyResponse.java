package city.sane.wot.binding.websocket.message;

public class SubscribePropertyResponse extends AbstractMessage {


    private Object value;

    private SubscribePropertyResponse() {
        this.value = null;
    }

    public SubscribePropertyResponse(Object value) {
        this.value = value;
    }


    @Override
    public String getType() {
        return "subscribePropertyResponse";
    }

    public Object getValue() {
        return value;
    }


}
