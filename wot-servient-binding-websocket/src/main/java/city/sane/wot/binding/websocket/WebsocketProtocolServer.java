package city.sane.wot.binding.websocket;

import city.sane.wot.Servient;
import city.sane.wot.binding.ProtocolServer;
import city.sane.wot.binding.websocket.message.AbstractClientMessage;
import city.sane.wot.binding.websocket.message.ReadProperty;
import city.sane.wot.content.ContentManager;
import city.sane.wot.thing.ExposedThing;
import city.sane.wot.thing.action.ExposedThingAction;
import city.sane.wot.thing.event.ExposedThingEvent;
import city.sane.wot.thing.form.Form;
import city.sane.wot.thing.form.Operation;
import city.sane.wot.thing.property.ExposedThingProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.typesafe.config.Config;
import org.java_websocket.WebSocket;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.server.WebSocketServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.stream.Collectors;

public class WebsocketProtocolServer implements ProtocolServer {
    private static final ObjectMapper JSON_MAPPER = new ObjectMapper();

    private final Logger log = LoggerFactory.getLogger(WebsocketProtocolServer.class);

    private Map<String, ExposedThing> things;

    private ServientWebsocketServer server;
    private List<String> addresses;
    private int bindPort;

    public WebsocketProtocolServer(Config config) {
        bindPort = config.getInt("wot.servient.websocket.bind-port");
        server = new ServientWebsocketServer(new InetSocketAddress(bindPort));
        if (!config.getStringList("wot.servient.websocket.addresses").isEmpty()) {
            addresses = config.getStringList("wot.servient.websocket.addresses");
        } else {
            addresses = Servient.getAddresses().stream().map(a -> "ws://" + a + ":" + bindPort + "").collect(Collectors.toList());
        }
        things = new HashMap<>();
    }

    @Override
    public CompletableFuture<Void> start() {
        // FIXME: The server currently fails silently if it cannot be started. We need to rebuild this and throw an exception if the server can't be started.
        return CompletableFuture.runAsync(server::start);
    }

    @Override
    public CompletableFuture<Void> stop() {
        return CompletableFuture.runAsync(() -> {
            try {
                server.stop();
            } catch (IOException | InterruptedException e) {
                throw new CompletionException(e);
            }
        });
    }

    @Override
    public CompletableFuture<Void> expose(ExposedThing thing) {
        log.info("WebsocketServer exposes '{}'", thing.getTitle());
        things.put(thing.getId(), thing);

        for (String address : addresses) {
            exposeProperties(thing, address);
//            exposeActions(thing, address);
//            exposeEvents(thing, address);
        }

        return CompletableFuture.completedFuture(null);
    }

    @Override
    public CompletableFuture<Void> destroy(ExposedThing thing) {
        log.info("WebsocketServer stop exposing '{}'", thing.getTitle());
        things.remove(thing.getId());

        return CompletableFuture.completedFuture(null);
    }

    private void exposeProperties(ExposedThing thing, String address) {
//        Form allForm = new Form.Builder()
//                .setHref(address)
//                .setOp(Operation.READ_ALL_PROPERTIES, Operation.READ_MULTIPLE_PROPERTIES)
//                .build();
//        thing.addForm(allForm);
//        log.info("Assign '{}' for reading all properties", address);

        Map<String, ExposedThingProperty> properties = thing.getProperties();
        properties.forEach((name, property) -> {
            if (!property.isWriteOnly()) {
                property.addForm(new Form.Builder()
                        .setHref(address)
                        .setOp(Operation.READ_PROPERTY)
                        .setOptional("websocket:message", Map.of(
                                "type", "readProperty",
                                "thingId", thing.getId(),
                                "name", name
                        ))
                        .build());
                log.info("Assign '{}' to Property '{}'", address, name);
            }
            if (!property.isReadOnly()) {
                property.addForm(new Form.Builder()
                        .setHref(address)
                        .setOp(Operation.WRITE_PROPERTY)
                        .setOptional("websocket:message", Map.of(
                                "type", "writeProperty",
                                "thingId", thing.getId(),
                                "name", name
                        ))
                        .build());
                log.info("Assign '{}' to Property '{}'", address, name);
            }

            // if property is observable add an additional form with a observable href
//            if (property.isObservable()) {
//                Form.Builder observableForm = new Form.Builder();
//                observableForm.setHref(address);
//                observableForm.setContentType(contentType);
//                observableForm.setOp(Operation.OBSERVE_PROPERTY);
//
//                property.addForm(observableForm.build());
//                log.info("Assign '{}' to observable Property '{}'", address, name);
//            }
        });
    }

    private void exposeActions(ExposedThing thing, String address) {
        Map<String, ExposedThingAction> actions = thing.getActions();
        actions.forEach((name, action) -> {
            Form.Builder form = new Form.Builder();
            form.setHref(address);
            form.setOp(Operation.INVOKE_ACTION);

            action.addForm(form.build());
            log.info("Assign '{}' to Action '{}'", address, name);
        });
    }

    private void exposeEvents(ExposedThing thing, String address) {
        Map<String, ExposedThingEvent> events = thing.getEvents();
        events.forEach((name, event) -> {
            Form.Builder form = new Form.Builder();
            form.setHref(address);
            form.setOp(Operation.SUBSCRIBE_EVENT);

            event.addForm(form.build());
            log.info("Assign '{}' to Event '{}'", address, name);
        });
    }

    class ServientWebsocketServer extends WebSocketServer {
        ServientWebsocketServer(InetSocketAddress inetSocketAddress) {
            super(inetSocketAddress);
        }

        @Override
        public void onOpen(WebSocket conn, ClientHandshake handshake) {
            log.debug("New Websocket connectione has been opened");
        }

        @Override
        public void onClose(WebSocket conn, int code, String reason, boolean remote) {
            log.debug("WebsocketServer is closing");
        }

        @Override
        public void onMessage(WebSocket conn, String json) {
            log.info("Received message: {}", json);

            try {
                AbstractClientMessage message = JSON_MAPPER.readValue(json, AbstractClientMessage.class);
                log.debug("Deserialized message to: {}", message);

                message.reply(conn, things).whenComplete((response, e) -> {
                    if (e != null) {
                        // FIXME: handle exception
                    } else {
                        try {
                            String outputJson = JSON_MAPPER.writeValueAsString(response);
                            conn.send(outputJson);
                        } catch (JsonProcessingException ex) {
                            // FIXME: handle exception
                        }
                    }
                });
            } catch (IOException e) {
                log.warn("Error on deserialization of message: {}", json);
            }
        }

        @Override
        public void onError(WebSocket conn, Exception ex) {
            log.warn("An error occured on websocket server", ex);
        }

        @Override
        public void onStart() {
            log.debug("WebsocketServer has been started");
        }
    }
}
