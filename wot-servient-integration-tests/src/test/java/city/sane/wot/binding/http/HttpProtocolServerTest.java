package city.sane.wot.binding.http;

import city.sane.wot.thing.ExposedThing;
import city.sane.wot.thing.action.ThingAction;
import city.sane.wot.thing.event.ThingEvent;
import city.sane.wot.thing.property.ThingProperty;
import city.sane.wot.thing.security.SecurityScheme;
import com.typesafe.config.ConfigFactory;
import org.hamcrest.text.MatchesPattern;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutionException;

import static org.hamcrest.text.MatchesPattern.matchesPattern;
import static org.junit.Assert.*;


public class HttpProtocolServerTest {
    private HttpProtocolServer server;

    @Before
    public void setUp() {
        server = new HttpProtocolServer(ConfigFactory.load());
        server.start().join();
    }

    @After
    public void tearDown() {
        server.stop().join();
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

        assertThat(url, MatchesPattern.matchesPattern("http://.*:8080"));
    }

    @Test
    public void getThingUrl() {
        String url = server.getThingUrl("counter").toString();

        assertThat(url, MatchesPattern.matchesPattern("http://.*:8080/counter"));
    }

    @Test
    public void setSecurity() {
        HttpProtocolClient client = new HttpProtocolClient();

        SecurityScheme securityScheme = new SecurityScheme();
        securityScheme.setScheme("basic");
        List<SecurityScheme> metadata = Collections.singletonList(securityScheme);

        Object credentials = ConfigFactory
                .parseString("credentials { username = \"foo\"\npassword = \"bar\" }").getObject("credentials");

        assertTrue(client.setSecurity(metadata, credentials));
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

        ThingProperty sinkProperty = new ThingProperty.Builder()
                .setType("string")
                .setDescription("write only property")
                .setWriteOnly(true)
                .build();

        ExposedThing thing = new ExposedThing(null)
                .setId("counter")
                .setTitle("counter")
                .setDescription("counter example Thing");

        thing.addProperty("count", counterProperty, 42);
        thing.addProperty("lastChange", lastChangeProperty, new Date().toString());
        thing.addProperty("sink", sinkProperty, "Hello");

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