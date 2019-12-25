package city.sane.wot.binding;

import city.sane.wot.binding.akka.AkkaProtocolServer;
import city.sane.wot.binding.coap.CoapProtocolServer;
import city.sane.wot.binding.http.HttpProtocolServer;
import city.sane.wot.binding.jadex.JadexProtocolServer;
import city.sane.wot.binding.mqtt.MqttProtocolServer;
import city.sane.wot.thing.ExposedThing;
import city.sane.wot.thing.action.ThingAction;
import city.sane.wot.thing.event.ThingEvent;
import city.sane.wot.thing.property.ThingProperty;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutionException;

import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

@RunWith(Parameterized.class)
public class ProtocolServerIT {
    @Parameterized.Parameter
    public Class<? extends ProtocolServer> protocolServerClass;
    private ProtocolServer server;

    @Before
    public void setup() throws NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException {
        try {
            // initialize server with config
            server = protocolServerClass.getConstructor(Config.class).newInstance(ConfigFactory.load());
        }
        catch (NoSuchMethodException e) {
            // fallback: initialize server without config
            server = protocolServerClass.getConstructor().newInstance();
        }

        server.start().join();
    }

    @After
    public void teardown() {
        server.stop().join();
    }

    @Test
    public void duplicateStart() throws ExecutionException, InterruptedException {
        assertNull(server.start().get());
    }

    @Test
    public void duplicateStop() throws ExecutionException, InterruptedException {
        server.stop().join();
        assertNull(server.stop().get());
    }

    @Test
    public void expose() {
        ExposedThing thing = getCounterThing();
        server.expose(thing).join();

        assertTrue("There must be at least one form", !thing.getProperty("count").getForms().isEmpty());
        assertTrue("There must be at least one action", !thing.getAction("increment").getForms().isEmpty());
        assertTrue("There must be at least one event", !thing.getEvent("change").getForms().isEmpty());
    }

    @Test
    public void destroy() throws ExecutionException, InterruptedException {
        ExposedThing thing = getCounterThing();
        server.expose(thing).join();
        server.destroy(thing).join();

        assertNull(server.destroy(thing).get());
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

    @Parameterized.Parameters(name = "{0}")
    public static List<Class<? extends ProtocolServer>> data() {
        return Arrays.asList(
                AkkaProtocolServer.class,
                CoapProtocolServer.class,
                HttpProtocolServer.class,
                JadexProtocolServer.class,
                MqttProtocolServer.class
        );
    }
}