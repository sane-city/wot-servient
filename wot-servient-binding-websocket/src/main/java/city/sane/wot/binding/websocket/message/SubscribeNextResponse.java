package city.sane.wot.binding.websocket.message;

import city.sane.wot.content.Content;

import java.util.Objects;

public class SubscribeNextResponse extends AbstractServerMessage {
    private final Content next;

    private SubscribeNextResponse() {
        super();
        next = null;
    }

    public SubscribeNextResponse(String id, Content next) {
        super(id);
        this.next = Objects.requireNonNull(next);
    }

    @Override
    public Content toContent() {
        return next;
    }

    public Content getNext() {
        return next;
    }
}
