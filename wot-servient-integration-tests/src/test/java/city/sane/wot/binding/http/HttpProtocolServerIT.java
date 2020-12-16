package city.sane.wot.binding.http;

import city.sane.wot.binding.ProtocolServerException;
import city.sane.wot.thing.ExposedThing;
import city.sane.wot.thing.action.ThingAction;
import city.sane.wot.thing.event.ThingEvent;
import city.sane.wot.thing.property.ThingProperty;
import city.sane.wot.thing.security.BasicSecurityScheme;
import city.sane.wot.thing.security.SecurityScheme;
import com.typesafe.config.ConfigFactory;
import org.hamcrest.MatcherAssert;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;

import static org.hamcrest.text.MatchesPattern.matchesPattern;
import static org.junit.Assert.assertTrue;

public class HttpProtocolServerIT {
    private HttpProtocolServer server;

    @BeforeEach
    public void setUp() throws ProtocolServerException {
        server = new HttpProtocolServer(ConfigFactory.load());
        server.start(null).join();
    }

    @AfterEach
    public void tearDown() {
        server.stop().join();
    }

    @Test
    public void getDirectoryUrl() {
        String url = server.getDirectoryUrl().toString();

        MatcherAssert.assertThat(url, matchesPattern("http://.*:\\d+"));
    }

    @Test
    public void getThingUrl() {
        String url = server.getThingUrl("counter").toString();

        MatcherAssert.assertThat(url, matchesPattern("http://.*:\\d+/counter"));
    }

    @Test
    public void setSecurity() {
        HttpProtocolClient client = new HttpProtocolClient();

        SecurityScheme securityScheme = new BasicSecurityScheme();
        List<SecurityScheme> metadata = Collections.singletonList(securityScheme);

        assertTrue(client.setSecurity(metadata, Map.of("username", "foo", "password", "bar")));
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

        thing.addAction("increment", new ThingAction<Object, Object>(), (input, options) -> {
            return thing.getProperty("count").read().thenApply(value -> {
                int newValue = ((Integer) value) + 1;
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
}