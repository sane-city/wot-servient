import city.sane.wot.binding.ProtocolClient;
import city.sane.wot.binding.ProtocolClientException;
import city.sane.wot.binding.ProtocolClientFactory;
import com.typesafe.config.Config;

import java.util.concurrent.CompletableFuture;

/**
 * Creates new {@link WebsocketProtocolClient} instances.
 */
public class WebsocketProtocolClientFactory implements ProtocolClientFactory {
    private final Config config;

    public WebsocketProtocolClientFactory(Config config) {
        this.config = config;
    }

    @Override
    public String getScheme() {
        return "websocket";
    }

    @Override
    public ProtocolClient getClient() throws ProtocolClientException {
        return new WebsocketProtocolClient(config);
    }
}
