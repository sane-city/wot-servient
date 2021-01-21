package city.sane.wot.binding.websocket;

import city.sane.Pair;
import city.sane.wot.binding.websocket.message.AbstractClientMessage;
import city.sane.wot.binding.websocket.message.AbstractServerMessage;
import city.sane.wot.binding.websocket.message.ClientErrorResponse;
import city.sane.wot.binding.websocket.message.InvokeAction;
import city.sane.wot.binding.websocket.message.InvokeActionResponse;
import city.sane.wot.binding.websocket.message.ReadProperty;
import city.sane.wot.binding.websocket.message.ReadPropertyResponse;
import city.sane.wot.binding.websocket.message.SubscribeCompleteResponse;
import city.sane.wot.binding.websocket.message.SubscribeErrorResponse;
import city.sane.wot.binding.websocket.message.SubscribeEvent;
import city.sane.wot.binding.websocket.message.SubscribeNextResponse;
import city.sane.wot.binding.websocket.message.SubscribeProperty;
import city.sane.wot.binding.websocket.message.WriteProperty;
import city.sane.wot.binding.websocket.message.WritePropertyResponse;
import city.sane.wot.content.Content;
import city.sane.wot.content.ContentCodecException;
import city.sane.wot.content.ContentManager;
import city.sane.wot.thing.ExposedThing;
import city.sane.wot.thing.action.ThingAction;
import city.sane.wot.thing.event.ExposedThingEvent;
import city.sane.wot.thing.event.ThingEvent;
import city.sane.wot.thing.property.ExposedThingProperty;
import city.sane.wot.thing.property.ThingProperty;
import city.sane.wot.thing.schema.NumberSchema;
import city.sane.wot.thing.schema.ObjectSchema;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.typesafe.config.ConfigFactory;
import io.reactivex.rxjava3.annotations.NonNull;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.subjects.PublishSubject;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

import java.io.IOException;
import java.net.URI;
import java.time.Duration;
import java.util.Date;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static org.awaitility.Awaitility.await;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertEquals;

public class WebsocketProtocolServerIT {
    private static final ObjectMapper JSON_MAPPER = new ObjectMapper();
    private WebsocketProtocolServer server;
    private ExposedThing thing;

    @BeforeEach
    public void setup() {
        server = new WebsocketProtocolServer(ConfigFactory.load());
        server.start(null).join();

        thing = getCounterThing();
        server.expose(thing).join();
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
                else if (options.containsKey("uriVariables") && options.get("uriVariables").containsKey("step")) {
                    step = (int) options.get("uriVariables").get("step");
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

        thing.addAction("decrement", new ThingAction<Object, Object>(), (input, options) -> {
            return thing.getProperty("count").read().thenApply(value -> {
                int newValue = ((Integer) value) - 1;
                thing.getProperty("count").write(newValue);
                thing.getProperty("lastChange").write(new Date().toString());
                thing.getEvent("change").emit();
                return newValue;
            });
        });

        thing.addAction("reset", new ThingAction<Object, Object>(), (input, options) -> {
            return thing.getProperty("count").write(0).thenApply(value -> {
                thing.getProperty("lastChange").write(new Date().toString());
                thing.getEvent("change").emit();
                return 0;
            });
        });

        thing.addEvent("change", new ThingEvent<Object>());

        return thing;
    }

    @AfterEach
    public void tearDown() {
        server.stop().join();
    }

    @Test
    @Timeout(20 * 1000L)
    public void testReadProperty() throws ContentCodecException {
        // send ReadProperty message to server and wait for ReadPropertyResponse message from server
        ReadProperty request = new ReadProperty("counter", "count");
        ReadProperty request2 = new ReadProperty("zähler", "count");
        ReadProperty request3 = new ReadProperty("counter", "mist");

        AbstractServerMessage response = ask(request);
        AbstractServerMessage response2 = ask(request2);
        AbstractServerMessage response3 = ask(request3);

        assertThat(response, instanceOf(ReadPropertyResponse.class));
        assertThat(response2, instanceOf(ClientErrorResponse.class));
        assertEquals("Thing not found", ((ClientErrorResponse) response2).getReason());
        assertThat(response3, instanceOf(ClientErrorResponse.class));
        assertEquals("Property not found", ((ClientErrorResponse) response3).getReason());
        assertEquals(request.getId(), response.getId());
        assertEquals(ContentManager.valueToContent(42), ((ReadPropertyResponse) response).getContent());
    }

    /**
     * Sends the message in <code>request</code> to the server and waits for the first response.
     *
     * @param request
     * @return
     */
    private AbstractServerMessage ask(AbstractClientMessage request) {
        return messageObserver(request).firstElement().blockingGet();
    }

    /**
     * Sends the message in <code>request</code> to the server and waits for the responses.
     *
     * @param request
     * @return
     */
    @NonNull
    private Observable<AbstractServerMessage> messageObserver(AbstractClientMessage request) {
        return Observable.using(
                () -> {
                    PublishSubject<AbstractServerMessage> subject = PublishSubject.create();
                    WebSocketClient client = new WebSocketClient(new URI("ws://localhost:8081")) {
                        @Override
                        public void onOpen(ServerHandshake handshake) {
                            try {
                                String json = WebsocketProtocolServerIT.JSON_MAPPER.writeValueAsString(request);
                                send(json);
                            }
                            catch (JsonProcessingException e) {
                                subject.onError(e);
                            }
                        }

                        @Override
                        public void onMessage(String json) {
                            try {
                                AbstractServerMessage message = JSON_MAPPER.readValue(json, AbstractServerMessage.class);
                                subject.onNext(message);
                            }
                            catch (IOException e) {
                                subject.onError(e);
                            }
                        }

                        @Override
                        public void onClose(int code, String reason, boolean remote) {
                            subject.onComplete();
                        }

                        @Override
                        public void onError(Exception ex) {
                            subject.onError(ex);
                        }
                    };
                    client.connect();

                    return new Pair<>(subject, client);
                },
                Pair::first,
                pair -> pair.second().close()
        );
    }

    @Test
    @Timeout(20 * 1000L)
    public void testWriteProperty() throws ContentCodecException {
        // send WriteProperty message to server and wait for WritePropertyResponse message from server
        WriteProperty request = new WriteProperty("counter", "count", ContentManager.valueToContent(1337));
        WriteProperty request2 = new WriteProperty("Null", "count", ContentManager.valueToContent(1337));
        WriteProperty request3 = new WriteProperty("counter", "Null", ContentManager.valueToContent(1337));

        AbstractServerMessage response = ask(request);
        AbstractServerMessage response2 = ask(request2);
        AbstractServerMessage response3 = ask(request3);

        assertThat(response, instanceOf(WritePropertyResponse.class));
        assertThat(response2, instanceOf(ClientErrorResponse.class));
        assertEquals("Thing not found", ((ClientErrorResponse) response2).getReason());
        assertThat(response3, instanceOf(ClientErrorResponse.class));
        assertEquals("Property not found", ((ClientErrorResponse) response3).getReason());
        assertEquals(request.getId(), response.getId());
        assertEquals(ContentManager.valueToContent(null), ((WritePropertyResponse) response).getValue());
    }

    @Test
    @Timeout(20 * 1000L)
    public void testInvokeAction() throws ContentCodecException {
        // send InvokeAction message to server and wait for InvokeActionResponse message from server
        Map<String, Integer> parameters = Map.of("step", 3);
        InvokeAction request = new InvokeAction("counter", "increment", ContentManager.valueToContent(parameters));

        AbstractServerMessage response = ask(request);

        assertThat(response, instanceOf(InvokeActionResponse.class));
        assertEquals(request.getId(), response.getId());
        assertEquals(ContentManager.valueToContent(45), ((InvokeActionResponse) response).getValue());
    }

    @Test
    public void testSubscribeProperty() throws ExecutionException, InterruptedException, ContentCodecException, TimeoutException {
        ExposedThingProperty<Object> property = thing.getProperty("count");

        // send SubscribeProperty message to server and wait for SubscribeNextResponse message from server
        SubscribeProperty request = new SubscribeProperty("counter", "count");
        Future<Content> future = subscription(request).firstElement().toFuture();

        // wait until client has established subscription
        await().atMost(Duration.ofSeconds(10)).until(property.getState().getSubject()::hasObservers);

        property.write(1337).get();

        assertEquals(ContentManager.valueToContent(1337), future.get(10, TimeUnit.SECONDS));
    }

    /**
     * Sends the subscribe message in <code>request</code> to the server and waits for the subscribe
     * responses.
     *
     * @param request
     * @return
     */
    private Observable<Content> subscription(AbstractClientMessage request) {
        return messageObserver(request).flatMap(message -> Observable.create(source -> {
            if (message instanceof SubscribeNextResponse) {
                source.onNext(message.toContent());
            }
            else if (message instanceof SubscribeCompleteResponse) {
                source.onComplete();
            }
            else if (message instanceof SubscribeErrorResponse) {
                source.onError(((SubscribeErrorResponse) message).getError());
            }
        }));
    }

    @Test
    public void testSubscribeEvent() throws ExecutionException, InterruptedException, TimeoutException {
        ExposedThingEvent<Object> event = thing.getEvent("change");
        // send SubscribeEvent message to server
        // and wait for SubscribeNextResponse message from server
        SubscribeEvent request = new SubscribeEvent("counter", "change");
        Future<Content> future = subscription(request).firstElement().toFuture();

        // wait until client has established subscription
        await().atMost(Duration.ofSeconds(10)).until(event.getState().getSubject()::hasObservers);

        event.emit();

        assertEquals(new Content(ContentManager.DEFAULT, "null".getBytes()), future.get(10, TimeUnit.SECONDS));
    }
}
