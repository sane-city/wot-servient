package city.sane.wot.binding.websocket;

import city.sane.wot.binding.ProtocolClientFactory;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

import static java.util.concurrent.CompletableFuture.completedFuture;

/**
 * Creates new {@link WebsocketProtocolClient} instances.
 */
public class WebsocketProtocolClientFactory implements ProtocolClientFactory {
    private final Set<WebsocketProtocolClient> clients = new HashSet<>();

    @Override
    public String getScheme() {
        return "ws";
    }

    @Override
    public WebsocketProtocolClient getClient() {
        WebsocketProtocolClient client = new WebsocketProtocolClient();
        clients.add(client);
        return client;
    }

    @Override
    public CompletableFuture<Void> destroy() {
        clients.forEach(WebsocketProtocolClient::destroy);
        return completedFuture(null);
    }
}
