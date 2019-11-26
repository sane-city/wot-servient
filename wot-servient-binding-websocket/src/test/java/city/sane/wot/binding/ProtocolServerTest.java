package city.sane.wot.binding;

import city.sane.wot.Servient;
import city.sane.wot.binding.websocket.WebsocketProtocolServer;
import city.sane.wot.thing.ExposedThing;
import city.sane.wot.thing.action.ThingAction;
import city.sane.wot.thing.event.ThingEvent;
import city.sane.wot.thing.property.ThingProperty;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import static org.junit.Assert.assertTrue;

@RunWith(Parameterized.class)
public class ProtocolServerTest {
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
    public void test() {
        try {
            servient.start().join();

            ExposedThing thing = getCounterThing();
            servient.addThing(thing);
            thing.expose().join();

            // TODO:
            cc = new WebSocketClient(new URI("ws://localhost:8080")) {

                @Override
                public void onMessage(String message) {
                    System.out.println("test2");
                }

                @Override
                public void onOpen(ServerHandshake handshake) {

                }

                @Override
                public void onClose(int code, String reason, boolean remote) {

                }

                @Override
                public void onError(Exception ex) {

                }
            };
            cc.connect();
            Thread.sleep(1000);
            cc.send("test1");


            System.out.println();
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
