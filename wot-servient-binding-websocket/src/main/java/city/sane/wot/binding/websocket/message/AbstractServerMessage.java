package city.sane.wot.binding.websocket.message;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

/**
 * Abstract class for all messages sent from {@link city.sane.wot.binding.websocket.WebsocketProtocolServer} to {@link city.sane.wot.binding.websocket.WebsocketProtocolClient}.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "type")
@JsonSubTypes({
        @JsonSubTypes.Type(value = ReadPropertyResponse.class, name = "readPropertyResponse"),
        @JsonSubTypes.Type(value = WritePropertyResponse.class, name = "writePropertyResponse"),
        @JsonSubTypes.Type(value = InvokeActionResponse.class, name = "invokeActionResponse")
})
public class AbstractServerMessage {
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
}
