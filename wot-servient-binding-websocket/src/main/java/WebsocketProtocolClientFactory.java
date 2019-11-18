import city.sane.wot.binding.ProtocolClient;
import city.sane.wot.binding.ProtocolClientException;
import city.sane.wot.binding.ProtocolClientFactory;

import java.util.concurrent.CompletableFuture;

public class WebsocketProtocolClientFactory implements ProtocolClientFactory {
    @Override
    public String getScheme() {
        return null;
    }

    @Override
    public ProtocolClient getClient() throws ProtocolClientException {
        return null;
    }

    @Override
    public CompletableFuture<Void> init() {
        return null;
    }

    @Override
    public CompletableFuture<Void> destroy() {
        return null;
    }
}
