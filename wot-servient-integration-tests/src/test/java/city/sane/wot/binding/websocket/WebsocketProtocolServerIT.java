package city.sane.wot.binding.websocket;

import city.sane.wot.binding.websocket.message.*;
import city.sane.wot.content.Content;
import city.sane.wot.content.ContentCodecException;
import city.sane.wot.content.ContentManager;
import city.sane.wot.thing.ExposedThing;
import city.sane.wot.thing.action.ThingAction;
import city.sane.wot.thing.event.ThingEvent;
import city.sane.wot.thing.property.ThingProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.typesafe.config.ConfigFactory;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Date;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.*;

public class WebsocketProtocolServerIT {
    private static final ObjectMapper JSON_MAPPER = new ObjectMapper();

    private WebsocketProtocolServer server;
    private WebSocketClient cc;
    private ExposedThing thing;

    @Before
    public void setup() {
        server = new WebsocketProtocolServer(ConfigFactory.load());
        server.start().join();

        thing = getCounterThing();
        server.expose(thing).join();
    }

    @After
    public void tearDown() {
        server.stop().join();
    }

    @Test(timeout = 20 * 1000)
    public void testReadProperty() throws ExecutionException, URISyntaxException, InterruptedException {
        // send ReadProperty message to server and wait for ReadPropertyResponse message from server
        ReadProperty request = new ReadProperty("counter", "count");

        AbstractServerMessage response = ask(request);

        assertThat(response, instanceOf(ReadPropertyResponse.class));
        assertEquals(request.getId(), response.getClientId());
        assertEquals(42, ((ReadPropertyResponse) response).getValue());
    }

    @Test(timeout = 20 * 1000)
    public void testWriteProperty() throws ExecutionException, InterruptedException, URISyntaxException, ContentCodecException {
        // send WriteProperty message to server and wait for WritePropertyResponse message from server
        Content payload = ContentManager.valueToContent(1337, "application/json");
        WriteProperty request = new WriteProperty("counter", "count", payload);

        AbstractServerMessage response = ask(request);

        assertThat(response, instanceOf(WritePropertyResponse.class));
        assertEquals(request.getId(), response.getClientId());
        assertNull(((WritePropertyResponse) response).getValue());
    }

    private ExposedThing getCounterThing() {
        ThingProperty counterProperty = new ThingProperty.Builder()
                .setType("integer")
                .setDescription("current counter value")
                .setObservable(true)
                .setReadOnly(true)
                .build();

        ThingProperty lastChangeProperty = new ThingProperty.Builder()
                .setType("string")
                .setDescription("last change of counter value")
                .setObservable(true)
                .setReadOnly(true)
                .build();

        ExposedThing thing = new ExposedThing(null)
                .setId("counter")
                .setTitle("counter")
                .setDescription("counter example Thing");

        thing.addProperty("count", counterProperty, 42);
        thing.addProperty("lastChange", lastChangeProperty, new Date().toString());

        thing.addAction("increment", new ThingAction(), (input, options) -> {
            return thing.getProperty("count").read().thenApply(value -> {
                int newValue = ((Integer) value) + 1;
                thing.getProperty("count").write(newValue);
                thing.getProperty("lastChange").write(new Date().toString());
                thing.getEvent("change").emit();
                return newValue;
            });
        });

        thing.addAction("decrement", new ThingAction(), (input, options) -> {
            return thing.getProperty("count").read().thenApply(value -> {
                int newValue = ((Integer) value) - 1;
                thing.getProperty("count").write(newValue);
                thing.getProperty("lastChange").write(new Date().toString());
                thing.getEvent("change").emit();
                return newValue;
            });
        });

        thing.addAction("reset", new ThingAction(), (input, options) -> {
            return thing.getProperty("count").write(0).thenApply(value -> {
                thing.getProperty("lastChange").write(new Date().toString());
                thing.getEvent("change").emit();
                return 0;
            });
        });

        thing.addEvent("change", new ThingEvent());

        return thing;
    }

    private AbstractServerMessage ask(AbstractClientMessage request) throws ExecutionException, InterruptedException, URISyntaxException {
        CompletableFuture<AbstractServerMessage> future = new CompletableFuture<>();

        try {
            cc = new WebSocketClient(new URI("ws://localhost:8080")) {
                @Override
                public void onOpen(ServerHandshake handshake) {
                    try {
                        String json = WebsocketProtocolServerIT.JSON_MAPPER.writeValueAsString(request);
                        cc.send(json);
                    }
                    catch (JsonProcessingException e) {
                        throw new RuntimeException(e);
                    }
                }

                @Override
                public void onMessage(String json) {
                    try {
                        AbstractServerMessage message = JSON_MAPPER.readValue(json, AbstractServerMessage.class);
                        future.complete(message);
                    }
                    catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }

                @Override
                public void onClose(int code, String reason, boolean remote) {
                    System.out.println("Tschüss");
                }

                @Override
                public void onError(Exception ex) {

                }
            };
            cc.connect();
        }
        finally {
            cc.close();
        }

        return future.get();
    }
}
