package city.sane.wot.binding.websocket;

import city.sane.wot.binding.ProtocolServer;
import city.sane.wot.thing.ExposedThing;

import java.util.concurrent.CompletableFuture;

public class WebsocketProtocolServer implements ProtocolServer {

    // TODO CompletableFuture start()
    @Override
    public CompletableFuture<Void> start() {
        return null;
    }

    //TODO CompletableFuture stop()
    @Override
    public CompletableFuture<Void> stop() {
        return null;
    }

    //TODO CompletableFuture expose()
    @Override
    public CompletableFuture<Void> expose(ExposedThing thing) {
        return null;
    }

    //TODO CompletableFuture destroy()
    @Override
    public CompletableFuture<Void> destroy(ExposedThing thing) {
        return null;
    }
}
