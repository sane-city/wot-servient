package city.sane.wot.binding.websocket;

import city.sane.wot.binding.ProtocolClient;
import city.sane.wot.binding.ProtocolClientFactory;
import org.java_websocket.client.WebSocketClient;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * Creates new {@link WebsocketProtocolClient} instances.
 */
public class WebsocketProtocolClientFactory implements ProtocolClientFactory {
    private Map<URI, WebSocketClient> clients = new HashMap<>();

    @Override
    public String getScheme() {
        return "ws";
    }

    @Override
    public ProtocolClient getClient() {
        return new WebsocketProtocolClient(clients);
    }

    @Override
    public CompletableFuture<Void> destroy() {
        return CompletableFuture.runAsync(() -> clients.values().forEach(WebSocketClient::close));
    }
}
