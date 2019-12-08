package city.sane.wot.binding.http.route;

import city.sane.wot.Servient;
import city.sane.wot.ServientException;
import city.sane.wot.thing.ExposedThing;
import city.sane.wot.thing.action.ThingAction;
import city.sane.wot.thing.event.ThingEvent;
import city.sane.wot.thing.property.ThingProperty;
import city.sane.wot.thing.schema.NumberSchema;
import city.sane.wot.thing.schema.ObjectSchema;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.entity.ContentType;
import org.apache.http.impl.client.HttpClientBuilder;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.util.Date;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class ReadAllPropertiesRouteTest {
    private static final ObjectMapper JSON_MAPPER = new ObjectMapper();

    private Servient servient;

    @Before
    public void setup() throws ServientException {
        Config config = ConfigFactory
                .parseString("wot.servient.servers = [\"city.sane.wot.binding.http.HttpProtocolServer\"]\n" +
                        "wot.servient.client-factories = []")
                .withFallback(ConfigFactory.load());
        servient = new Servient(config);
        servient.start().join();
    }

    @After
    public void teardown() {
        servient.shutdown().join();
    }

    @Test
    public void readAllProperties() throws IOException {
        ExposedThing thing = getCounterThing();
        servient.addThing(thing);
        servient.expose(thing.getId()).join();

        HttpUriRequest request = new HttpGet("http://localhost:8080/counter/all/properties");
        HttpResponse response = HttpClientBuilder.create().build().execute(request);

        assertEquals(200, response.getStatusLine().getStatusCode());
        assertEquals("application/json", ContentType.getOrDefault(response.getEntity()).getMimeType());
        assertTrue(
                "Should return map with \"count\" element",
                JSON_MAPPER.readValue(response.getEntity().getContent(), Map.class).containsKey("count")
        );
    }

    private ExposedThing getCounterThing() {
        ExposedThing thing = new ExposedThing(servient)
                .setId("counter")
                .setTitle("counter")
                .setDescription("counter example Thing");

        ThingProperty counterProperty = new ThingProperty.Builder()
                .setType("integer")
                .setDescription("current counter value")
                .setObservable(true)
                .build();

        ThingProperty lastChangeProperty = new ThingProperty.Builder()
                .setType("string")
                .setDescription("last change of counter value")
                .setObservable(true)
                .setReadOnly(true)
                .build();

        thing.addProperty("count", counterProperty, 42);
        thing.addProperty("lastChange", lastChangeProperty, "2019-07-25 00:35:36");

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
}