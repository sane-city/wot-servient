package city.sane.wot.binding.websocket.message;

import city.sane.wot.content.Content;

import java.util.Objects;

public class InvokeActionResponse extends AbstractServerMessage implements FinalResponse {
    private final Content value;

    private InvokeActionResponse() {
        super();
        value = null;
    }

    public InvokeActionResponse(String id, Content value) {
        super(id);
        this.value = Objects.requireNonNull(value);
    }

    @Override
    public Content toContent() {
        return getValue();
    }

    public Content getValue() {
        return value;
    }
}
