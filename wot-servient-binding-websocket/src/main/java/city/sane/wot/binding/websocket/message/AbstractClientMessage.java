package city.sane.wot.binding.websocket.message;

import city.sane.wot.thing.ExposedThing;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

import java.util.Map;
import java.util.UUID;
import java.util.function.Consumer;

/**
 * Abstract class for all messages sent from {@link city.sane.wot.binding.websocket.WebsocketProtocolClient}
 * to {@link city.sane.wot.binding.websocket.WebsocketProtocolServer}.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "type")
@JsonSubTypes({
        @JsonSubTypes.Type(value = ReadProperty.class, name = "ReadProperty"),
        @JsonSubTypes.Type(value = WriteProperty.class, name = "WriteProperty"),
        @JsonSubTypes.Type(value = InvokeAction.class, name = "InvokeAction"),
        @JsonSubTypes.Type(value = SubscribeProperty.class, name = "SubscribeProperty"),
        @JsonSubTypes.Type(value = SubscribeEvent.class, name = "SubscribeEvent")
})
public abstract class AbstractClientMessage {
    private final String id;

    protected AbstractClientMessage() {
        id = randomId();
    }

    /**
     * Generates a new random ID to make the message uniquely identifiable.
     *
     * @return
     */
    public static String randomId() {
        return UUID.randomUUID().toString().substring(0, 6);
    }

    public String getId() {
        return id;
    }

    /**
     * Creates the server's response to the request sent by the client.
     *
     * @param replyConsumer
     * @param things
     * @return
     */
    public abstract void reply(Consumer<AbstractServerMessage> replyConsumer,
                               Map<String, ExposedThing> things);
}
