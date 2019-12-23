package city.sane.wot.binding.websocket.message;

import city.sane.wot.content.Content;

import java.util.Objects;

public class ReadPropertyResponse extends AbstractServerMessage implements FinalResponse {
    private final Content content;

    private ReadPropertyResponse() {
        super();
        content = null;
    }

    @Override
    public Content toContent() {
        return getContent();
    }

    public ReadPropertyResponse(String id, Content content) {
        super(id);
        this.content = Objects.requireNonNull(content);
    }

    public ReadPropertyResponse(ReadProperty clientMessage, Content content) {
        super(clientMessage);
        this.content = content;
    }

    public Content getContent() {
        return content;
    }
}
