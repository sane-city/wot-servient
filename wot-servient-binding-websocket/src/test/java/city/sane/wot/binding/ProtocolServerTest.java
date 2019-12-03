package city.sane.wot.binding;

import city.sane.wot.Servient;
import city.sane.wot.binding.websocket.WebsocketProtocolServer;
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
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

@RunWith(Parameterized.class)
public class ProtocolServerTest {
    private static final ObjectMapper JSON_MAPPER = new ObjectMapper();
    @Parameterized.Parameter
    public Class<? extends ProtocolServer> protocolServerClass;
    private Servient servient;
    private WebSocketClient cc;

    @Parameterized.Parameters(name = "{0}")
    public static List<Class<? extends ProtocolServer>> data() {
        return Arrays.asList(
                WebsocketProtocolServer.class
        );
    }

    @Before
    public void setup() {
        Config config = ConfigFactory
                .parseString("wot.servient.servers = [\"" + protocolServerClass.getName() + "\"]\n" +
                        "wot.servient.client-factories = []")
                .withFallback(ConfigFactory.load());

        servient = new Servient(config);
    }

    @Test
    public void test1() throws ExecutionException {
        try {
            servient.start().join();

            ExposedThing thing = getCounterThing();
            servient.addThing(thing);
            thing.expose().join();

            CompletableFuture<Object> future = new CompletableFuture<>();

            cc = new WebSocketClient(new URI("ws://localhost:8080")) {


                @Override
                public void onMessage(String json) {
                    try {
                        AbstractMessage message;
                        message = JSON_MAPPER.readValue(json, AbstractMessage.class);

                        if (message instanceof ReadPropertyResponse) {
                            ReadPropertyResponse rMessage = (ReadPropertyResponse) message;
                            future.complete(rMessage.getValue());
                        }

                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void onOpen(ServerHandshake handshake) {
                    AbstractMessage message = new ReadProperty("counter", "count");
                    try {
                        String json = ProtocolServerTest.JSON_MAPPER.writeValueAsString(message);
                        cc.send(json);
                    } catch (JsonProcessingException e) {
                        e.printStackTrace();
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

            assertEquals(42, future.get());
            System.out.println(future.get());
        } catch (URISyntaxException | InterruptedException e) {
            e.printStackTrace();
        } finally {
            servient.shutdown().join();
        }
    }

    @Test
    public void test2() throws ExecutionException {
        try {
            servient.start().join();

            ExposedThing thing = getCounterThing();
            servient.addThing(thing);
            thing.expose().join();

            CompletableFuture<Object> future2 = new CompletableFuture<>();

            cc = new WebSocketClient(new URI("ws://localhost:8080")) {

                private Content payload;

                @Override
                public void onMessage(String json) {
                    try {
                        System.out.println("onMessage");
                        AbstractMessage message;
                        message = JSON_MAPPER.readValue(json, AbstractMessage.class);

                        if (message instanceof WritePropertyResponse) {
                            System.out.println("WritePropertyResponse");
                            WritePropertyResponse wMessage = (WritePropertyResponse) message;
                            future2.complete(wMessage.getValue());
                        }

                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void onOpen(ServerHandshake handshake) {
                    try {
                        payload = ContentManager.valueToContent(1337, "none");
                    } catch (ContentCodecException e) {
                        e.printStackTrace();
                    }
                    AbstractMessage message2 = new WriteProperty("counter", "count", payload);
                    System.out.println("let's go message2");
                    String json = null;
                    try {
                        json = ProtocolServerTest.JSON_MAPPER.writeValueAsString(message2);
                    } catch (JsonProcessingException e) {
                        e.printStackTrace();
                    }
                    cc.send(json);


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

            assertEquals(1337, future2.get());
            System.out.println(future2.get());
        } catch (URISyntaxException | InterruptedException e) {
            e.printStackTrace();
        } finally {
            servient.shutdown().join();
        }
    }

    @Test
    public void test3() throws ExecutionException {
        try {
            servient.start().join();

            ExposedThing thing = getCounterThing();
            servient.addThing(thing);
            thing.expose().join();

            CompletableFuture<Object> future3 = new CompletableFuture<>();

            // TODO:
            cc = new WebSocketClient(new URI("ws://localhost:8080")) {

                @Override
                public void onMessage(String json) {
                    try {
                        System.out.println("onMessage");
                        AbstractMessage message;
                        message = JSON_MAPPER.readValue(json, AbstractMessage.class);

                        if (message instanceof SubscribePropertyResponse) {
                            System.out.println("SubscribePropertyResponse");
                            SubscribePropertyResponse sMessage = (SubscribePropertyResponse) message;
                            future3.complete(sMessage.getValue());
                        }

                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void onOpen(ServerHandshake handshake) {
                    AbstractMessage message3 = new SubscribeProperty("counter", "count");
                    System.out.println("let's go message3");
                    String json = null;
                    try {
                        json = ProtocolServerTest.JSON_MAPPER.writeValueAsString(message3);
                    } catch (JsonProcessingException e) {
                        e.printStackTrace();
                    }
                    cc.send(json);


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

            System.out.println(future3.get());
        } catch (URISyntaxException | InterruptedException e) {
            e.printStackTrace();
        } finally {
            servient.shutdown().join();
        }
    }


    @Test
    public void destroy() {
        try {
            servient.start().join();

            ExposedThing thing = getCounterThing();
            servient.addThing(thing);
            thing.expose().join();
            thing.destroy().join();

            assertTrue("There must be no forms", thing.getProperty("count").getForms().isEmpty());
            assertTrue("There must be no actions", thing.getAction("increment").getForms().isEmpty());
            assertTrue("There must be no events", thing.getEvent("change").getForms().isEmpty());
        } finally {
            servient.shutdown().join();
        }
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

        ExposedThing thing = new ExposedThing(servient)
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
}
