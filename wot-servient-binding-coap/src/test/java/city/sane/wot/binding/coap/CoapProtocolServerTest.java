package city.sane.wot.binding.coap;

import city.sane.wot.thing.ExposedThing;
import city.sane.wot.thing.action.ThingAction;
import city.sane.wot.thing.event.ThingEvent;
import city.sane.wot.thing.property.ThingProperty;
import com.typesafe.config.ConfigFactory;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.Date;
import java.util.concurrent.ExecutionException;

import static org.hamcrest.Matchers.matchesPattern;
import static org.junit.Assert.*;

public class CoapProtocolServerTest {
    private CoapProtocolServer server;

    @Before
    public void setUp() {
        server = new CoapProtocolServer(ConfigFactory.load());
        server.start().join();
    }

    @After
    public void tearDown() {
        server.stop().join();

        // TODO: Wait some time after the server has shut down. Apparently the CoAP server reports too early that it was terminated, even though the port is
        //  still in use. This sometimes led to errors during the tests because other CoAP servers were not able to be started because the port was already
        //  in use. This error only occurred in the GitLab CI (in Docker). Instead of waiting, the error should be reported to the maintainer of the CoAP
        //  server and fixed. Because the isolation of the error is so complex, this workaround was chosen.
        try {
            Thread.sleep(1 * 1000L);
        }
        catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
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

        assertNull(server.destroy(thing).get());
    }

    @Test
    public void getDirectoryUrl() {
        String url = server.getDirectoryUrl().toString();

        assertThat(url, matchesPattern("coap://.*:5683"));
    }

    @Test
    public void getThingUrl() {
        String url = server.getThingUrl("counter").toString();

        assertThat(url, matchesPattern("coap://.*:5683/counter"));
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
}