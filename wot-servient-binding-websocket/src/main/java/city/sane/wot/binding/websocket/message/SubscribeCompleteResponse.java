package city.sane.wot.binding.websocket.message;

import city.sane.wot.content.Content;

public class SubscribeCompleteResponse extends AbstractServerMessage implements FinalResponse {
    private SubscribeCompleteResponse() {
        super();
    }

    public SubscribeCompleteResponse(String id) {
        super(id);
    }

    @Override
    public Content toContent() {
        return null;
    }
}
