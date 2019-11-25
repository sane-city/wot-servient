package city.sane.wot.binding.websocket;

import city.sane.wot.Servient;
import city.sane.wot.binding.ProtocolServer;
import city.sane.wot.content.ContentManager;
import city.sane.wot.thing.ExposedThing;
import city.sane.wot.thing.form.Form;
import city.sane.wot.thing.form.Operation;
import org.java_websocket.WebSocket;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.server.WebSocketServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

public class WebsocketProtocolServer implements ProtocolServer {
    private final static Logger log = LoggerFactory.getLogger(WebsocketProtocolServer.class);

    private final Map<String, ExposedThing> things = new HashMap<>();

    private final MyServer server;
    private final List<String> addresses;

    public WebsocketProtocolServer() {
        server = new MyServer(new InetSocketAddress(8080));
        addresses = Servient.getAddresses().stream().map(a -> "http://" + a + ":8080/things").collect(Collectors.toList());

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
        for (String address : addresses) {
            String href = address + "/" + thing.getId() + "/all/properties";
            Form form = new Form.Builder()
                    .setHref(href)
                    .setContentType(ContentManager.DEFAULT)
                    .setOp(Arrays.asList(Operation.readallproperties, Operation.readmultipleproperties))
                    .build();
            thing.addForm(form);
        }

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
