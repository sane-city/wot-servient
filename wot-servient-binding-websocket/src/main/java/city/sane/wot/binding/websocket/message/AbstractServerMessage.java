package city.sane.wot.binding.websocket.message;

import city.sane.wot.content.Content;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

/**
 * Abstract class for all messages sent from {@link city.sane.wot.binding.websocket.WebsocketProtocolServer} to {@link city.sane.wot.binding.websocket.WebsocketProtocolClient}.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "type")
@JsonSubTypes({
        @JsonSubTypes.Type(value = ReadPropertyResponse.class, name = "ReadPropertyResponse"),
        @JsonSubTypes.Type(value = WritePropertyResponse.class, name = "WritePropertyResponse"),
        @JsonSubTypes.Type(value = InvokeActionResponse.class, name = "InvokeActionResponse"),
        @JsonSubTypes.Type(value = SubscribeNextResponse.class, name = "SubscribeNextResponse"),
        @JsonSubTypes.Type(value = SubscribeErrorResponse.class, name = "SubscribeErrorResponse"),
        @JsonSubTypes.Type(value = SubscribeCompleteResponse.class, name = "SubscribeCompleteResponse"),
        @JsonSubTypes.Type(value = ServerErrorResponse.class, name = "ServerErrorResponse"),
        @JsonSubTypes.Type(value = ClientErrorResponse.class, name = "ClientErrorResponse")

})
public abstract class AbstractServerMessage {
    protected final String id;

    public AbstractServerMessage(String id) {
        this.id = id;
    }

    public AbstractServerMessage(AbstractClientMessage message) {
        this(message.getId());
    }

    protected AbstractServerMessage() {
        id = null;
    }

    public String getId() {
        return id;
    }

    public abstract Content toContent();
}
