package city.sane.wot.binding.websocket.message;

import city.sane.wot.thing.ExposedThing;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import org.java_websocket.WebSocket;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

/**
 * Abstract class for all messages sent from {@link city.sane.wot.binding.websocket.WebsocketProtocolClient} to {@link city.sane.wot.binding.websocket.WebsocketProtocolServer}.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "type")
@JsonSubTypes({
        @JsonSubTypes.Type(value = ReadProperty.class, name = "readProperty"),
        @JsonSubTypes.Type(value = WriteProperty.class, name = "writeProperty")
})
public abstract class AbstractClientMessage {
    private String id;

    public AbstractClientMessage() {
        id = randomId();
    }

    public static String randomId() {
        return UUID.randomUUID().toString().substring(0, 6);
    }

    public String getId() {
        return id;
    }

    /**
     * Creates the server's response to the request sent by the client.
     *
     * @param socket
     * @param things
     * @return
     */
    public abstract CompletableFuture<AbstractServerMessage> reply(WebSocket socket, Map<String, ExposedThing> things);
}
