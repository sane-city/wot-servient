package city.sane.wot.binding.websocket;

import city.sane.wot.Servient;
import city.sane.wot.binding.ProtocolServer;
import city.sane.wot.content.ContentManager;
import city.sane.wot.thing.ExposedThing;
import city.sane.wot.thing.ThingInteraction;
import city.sane.wot.thing.action.ExposedThingAction;
import city.sane.wot.thing.event.ExposedThingEvent;
import city.sane.wot.thing.form.Form;
import city.sane.wot.thing.form.Operation;
import city.sane.wot.thing.property.ExposedThingProperty;
import com.typesafe.config.Config;
import org.java_websocket.WebSocket;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.server.WebSocketServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

public class WebsocketProtocolServer implements ProtocolServer {
    private final static Logger log = LoggerFactory.getLogger(WebsocketProtocolServer.class);

    private final Map<String, ExposedThing> things = new HashMap<>();

    private final MyServer server;
    private final List<String> addresses;

    public WebsocketProtocolServer(Config config) {
        server = new MyServer(new InetSocketAddress(8080));
        addresses = Servient.getAddresses().stream().map(a -> "ws://" + a + ":8080").collect(Collectors.toList());

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
            for (String contentType : ContentManager.getOfferedMediaTypes()) {

                // properties

                Form formA = new Form.Builder()
                        .setHref(address)
                        .setContentType(contentType)
                        .setOp(Arrays.asList(Operation.readallproperties, Operation.readmultipleproperties))
                        .build();
                thing.addForm(formA);
                log.info("Assign '{}' for reading all properties", address);

                Map<String, ExposedThingProperty> properties = thing.getProperties();
                properties.forEach((name, property) -> {
                    Form.Builder formP = new Form.Builder();
                    formP.setHref(address);
                    formP.setContentType(contentType);
                    if (property.isReadOnly()) {
                        formP.setOp(Operation.readproperty);
                        // TODO
                        //formP.setOptional("htv:methodName", "GET");
                    } else if (property.isWriteOnly()) {
                        formP.setOp(Operation.writeproperty);
                        // TODO
                        //formP.setOptional("htv:methodName", "PUT");
                    } else {
                        formP.setOp(Arrays.asList(Operation.readproperty, Operation.writeproperty));
                    }

                    property.addForm(formP.build());
                    log.info("Assign '{}' to Property '{}'", address, name);

                    // if property is observable add an additional form with a observable href
                    if (property.isObservable()) {
                        Form.Builder observableForm = new Form.Builder();
                        observableForm.setHref(address);
                        observableForm.setContentType(contentType);
                        observableForm.setOp(Operation.observeproperty);

                        property.addForm(observableForm.build());
                        log.info("Assign '{}' to observable Property '{}'", address, name);
                    }
                });

                // actions

                Map<String, ExposedThingAction> actions = thing.getActions();
                actions.forEach((name, action) -> {
                    Form.Builder form = new Form.Builder();
                    form.setHref(address);
                    form.setContentType(contentType);
                    form.setOp(Operation.invokeaction);
                    // TODO
                    // form.setOptional("htv:methodName", "POST");

                    action.addForm(form.build());
                    log.info("Assign '{}' to Action '{}'", address, name);
                });

                // events

                Map<String, ExposedThingEvent> events = thing.getEvents();
                events.forEach((name, event) -> {
                    String href = getHrefWithVariablePattern(address, thing, "events", name, event);
                    Form.Builder form = new Form.Builder();
                    form.setHref(href);
                    form.setContentType(contentType);
                    form.setSubprotocol("longpoll");
                    form.setOp(Operation.subscribeevent);

                    event.addForm(form.build());
                    log.info("Assign '{}' to Event '{}'", href, name);
                });
            }
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

    private String getHrefWithVariablePattern(String address, ExposedThing thing, String type, String interactionName, ThingInteraction interaction) {
        String variables = "";
        Set<String> uriVariables = interaction.getUriVariables().keySet();
        if (!uriVariables.isEmpty()) {
            variables = "{?" + String.join(",", uriVariables) + "}";
        }

        return address + "/" + thing.getId() + "/" + type + "/" + interactionName + variables;
    }

    static class MyServer extends WebSocketServer {
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
            System.out.println("Nachricht erhalten: " + s);
        }

        @Override
        public void onError(WebSocket webSocket, Exception e) {

        }

        @Override
        public void onStart() {

        }
    }
}
