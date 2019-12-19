package city.sane.wot.binding.websocket.message;

import city.sane.wot.content.Content;

import java.util.Objects;

public class InvokeActionResponse extends AbstractServerMessage {
    private final Content value;

    private InvokeActionResponse() {
        super();
        value = null;
    }

    @Override
    public Content toContent() {
        return getValue();
    }

    public InvokeActionResponse(String id, Content value) {
        super(id);
        this.value = Objects.requireNonNull(value);
    }

    public InvokeActionResponse(ReadProperty clientMessage, Content value) {
        super(clientMessage);
        this.value = value;
    }

    public Content getValue() {
        return value;
    }
}
