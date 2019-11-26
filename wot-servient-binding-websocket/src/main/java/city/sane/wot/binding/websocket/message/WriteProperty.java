package city.sane.wot.binding.websocket.message;

public class WriteProperty extends AbstractMessage {
    @Override
    public String getType() {
        return "writeProperty";
    }
}
