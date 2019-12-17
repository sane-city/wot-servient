package city.sane.wot.binding.websocket.message;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "type")
@JsonSubTypes({
        @JsonSubTypes.Type(value = ReadPropertyResponse.class, name = "readPropertyResponse"),
        @JsonSubTypes.Type(value = WritePropertyResponse.class, name = "writePropertyResponse")
})
public class AbstractServerMessage {
    protected String clientId;

    public AbstractServerMessage() {
        clientId = null;
    }

    public AbstractServerMessage(String clientId) {
        this.clientId = clientId;
    }

    public AbstractServerMessage(AbstractClientMessage message) {
        this(message.getId());
    }

    public String getClientId() {
        return clientId;
    }
}
