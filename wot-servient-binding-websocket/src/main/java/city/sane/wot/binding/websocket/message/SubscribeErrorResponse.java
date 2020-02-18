package city.sane.wot.binding.websocket.message;

import city.sane.wot.content.Content;

public class SubscribeErrorResponse extends AbstractServerMessage implements FinalResponse {
    private final Throwable error;

    private SubscribeErrorResponse() {
        super();
        error = null;
    }

    @Override
    public Content toContent() {
        return null;
    }

    public Throwable getError() {
        return error;
    }
}
