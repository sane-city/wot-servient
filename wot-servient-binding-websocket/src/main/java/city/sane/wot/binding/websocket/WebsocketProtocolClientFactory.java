package city.sane.wot.binding.websocket;

import city.sane.wot.binding.ProtocolClient;
import city.sane.wot.binding.ProtocolClientException;
import city.sane.wot.binding.ProtocolClientFactory;
import com.typesafe.config.Config;

import java.net.URISyntaxException;

/**
 * Creates new {@link WebsocketProtocolClient} instances.
 */
public class WebsocketProtocolClientFactory implements ProtocolClientFactory {
    private Config config;

    public WebsocketProtocolClientFactory(Config config) {
        this.config = config;
    }

    @Override
    public String getScheme() {
        return "ws";
    }

    @Override
    public ProtocolClient getClient() throws ProtocolClientException {
        try {
            return new WebsocketProtocolClient(config);
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
        return null;
    }
}
