package city.sane.wot.binding.websocket;

import city.sane.wot.binding.websocket.message.*;
import city.sane.wot.content.Content;
import city.sane.wot.content.ContentCodecException;
import city.sane.wot.content.ContentManager;
import city.sane.wot.thing.ExposedThing;
import city.sane.wot.thing.action.ThingAction;
import city.sane.wot.thing.event.ThingEvent;
import city.sane.wot.thing.property.ThingProperty;
import city.sane.wot.thing.schema.NumberSchema;
import city.sane.wot.thing.schema.ObjectSchema;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.typesafe.config.ConfigFactory;
import org.eclipse.rdf4j.query.algebra.evaluation.function.numeric.Abs;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Date;
import java.util.Map;
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
    public void testReadProperty() throws ExecutionException, URISyntaxException, InterruptedException, ContentCodecException {
        // send ReadProperty message to server and wait for ReadPropertyResponse message from server
        ReadProperty request = new ReadProperty("counter", "count");
        ReadProperty request2 = new ReadProperty("zähler","count");
        ReadProperty request3 = new ReadProperty("counter","mist");


        AbstractServerMessage response = ask(request);
        AbstractServerMessage response2 = ask(request2);
        AbstractServerMessage response3 = ask(request3);

        assertThat(response, instanceOf(ReadPropertyResponse.class));
        assertThat(response2, instanceOf(ClientErrorResponse.class));
        assertEquals("404 Thing not found",((ClientErrorResponse) response2).getReason());
        assertThat(response3, instanceOf(ClientErrorResponse.class));
        assertEquals("404 Property not found",((ClientErrorResponse) response3).getReason());
        assertEquals(request.getId(), response.getId());
        assertEquals(ContentManager.valueToContent(42), ((ReadPropertyResponse) response).getValue());
    }

    @Test(timeout = 20 * 1000)
    public void testWriteProperty() throws ExecutionException, InterruptedException, URISyntaxException, ContentCodecException {
        // send WriteProperty message to server and wait for WritePropertyResponse message from server
        WriteProperty request = new WriteProperty("counter", "count", ContentManager.valueToContent(1337));

        AbstractServerMessage response = ask(request);

        assertThat(response, instanceOf(WritePropertyResponse.class));
        assertEquals(request.getId(), response.getId());
        assertEquals(ContentManager.valueToContent(null), ((WritePropertyResponse) response).getValue());
    }

    @Test //(timeout = 20 * 1000)
    public void testInvokeAction() throws ExecutionException, InterruptedException, URISyntaxException, ContentCodecException {
        // send InvokeAction message to server and wait for InvokeActionResponse message from server
        Map<String, Integer> parameters = Map.of("step", 3);
        InvokeAction request = new InvokeAction("counter", "increment", ContentManager.valueToContent(parameters));

        AbstractServerMessage response = ask(request);
        System.out.println(response);

        assertThat(response, instanceOf(InvokeActionResponse.class));
        assertEquals(request.getId(), response.getId());
        assertEquals(ContentManager.valueToContent(45), ((InvokeActionResponse) response).getValue());
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

        thing.addAction("increment", new ThingAction.Builder()
                .setInput(new ObjectSchema())
                .setOutput(new NumberSchema())
                .setUriVariables(Map.of(
                        "step", Map.of(
                                "type", "integer",
                                "minimum", 1,
                                "maximum", 250
                        )
                ))
                .build(), (input, options) -> {
            return thing.getProperty("count").read().thenApply(value -> {
                int step;
                if (input != null && ((Map) input).containsKey("step")) {
                    step = (Integer) ((Map) input).get("step");
                }
                else if (options.containsKey("uriVariables") && ((Map) options.get("uriVariables")).containsKey("step")) {
                    step = (int) ((Map) options.get("uriVariables")).get("step");
                }
                else {
                    step = 1;
                }

                int newValue = ((Integer) value) + step;
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

    /**
     * Sends the message in <code>request</code> to the server and waits for the response.
     *
     * @param request
     * @return
     * @throws ExecutionException
     * @throws InterruptedException
     * @throws URISyntaxException
     */
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
