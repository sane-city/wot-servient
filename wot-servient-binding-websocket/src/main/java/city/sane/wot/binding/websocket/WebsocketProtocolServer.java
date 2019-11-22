package city.sane.wot.binding.websocket;

import city.sane.wot.binding.ProtocolServer;
import city.sane.wot.thing.ExposedThing;
import org.java_websocket.WebSocket;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.server.WebSocketServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

public class WebsocketProtocolServer implements ProtocolServer {
    final static Logger log = LoggerFactory.getLogger(WebsocketProtocolServer.class);

    private final Map<String, ExposedThing> things = new HashMap<>();

    private final MyServer server;

    public WebsocketProtocolServer() {
        server = new MyServer( new InetSocketAddress( 8080 ));
    }

    @Override
    public CompletableFuture<Void> start() {
        return CompletableFuture.runAsync(server::start);
    }

    @Override
    public CompletableFuture<Void> stop() {
        return CompletableFuture.runAsync(() -> {
            try {
                server.stop();
            } catch (IOException | InterruptedException e) {
                e.printStackTrace();
            }
        });
    }

    @Override
    public CompletableFuture<Void> expose(ExposedThing thing) {
        log.info("WebsocketServer exposes '{}'", thing.getTitle());
        things.put(thing.getId(), thing);

        // TODO: add websocket forms to thing description

        return CompletableFuture.completedFuture(null);
    }

    @Override
    public CompletableFuture<Void> destroy(ExposedThing thing) {
        log.info("WebsocketServer stop exposing '{}'", thing.getTitle());
        things.remove(thing.getId());

        // TODO: remove websocket forms from thing description

        return CompletableFuture.completedFuture(null);
    }

    class MyServer extends WebSocketServer {
        public MyServer(InetSocketAddress inetSocketAddress) {
            super(inetSocketAddress);
        }

        @Override
        public void onOpen(WebSocket webSocket, ClientHandshake clientHandshake) {

        }

        @Override
        public void onClose(WebSocket webSocket, int i, String s, boolean b) {

        }

        @Override
        public void onMessage(WebSocket webSocket, String s) {
            // TODO: implementieren
            System.out.println("NAchricht erhalten: " + s);
        }

        @Override
        public void onError(WebSocket webSocket, Exception e) {

        }

        @Override
        public void onStart() {

        }
    }
}
