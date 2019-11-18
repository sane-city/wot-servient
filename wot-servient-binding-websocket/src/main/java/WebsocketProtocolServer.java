import city.sane.wot.binding.ProtocolServer;
import city.sane.wot.binding.ProtocolServerException;
import city.sane.wot.thing.ExposedThing;

import java.net.URI;
import java.util.concurrent.CompletableFuture;

public class WebsocketProtocolServer implements ProtocolServer {

    @Override
    public CompletableFuture<Void> start() {
        return null;
    }

    @Override
    public CompletableFuture<Void> stop() {
        return null;
    }

    @Override
    public CompletableFuture<Void> expose(ExposedThing thing) {
        return null;
    }

    @Override
    public CompletableFuture<Void> destroy(ExposedThing thing) {
        return null;
    }

    @Override
    public URI getDirectoryUrl() throws ProtocolServerException {
        return null;
    }

    @Override
    public URI getThingUrl(String id) throws ProtocolServerException {
        return null;
    }
}
