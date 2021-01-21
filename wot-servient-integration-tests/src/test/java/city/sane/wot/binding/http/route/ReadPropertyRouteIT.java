package city.sane.wot.binding.http.route;

import city.sane.wot.binding.http.ContentResponseTransformer;
import city.sane.wot.thing.ExposedThing;
import city.sane.wot.thing.action.ThingAction;
import city.sane.wot.thing.event.ThingEvent;
import city.sane.wot.thing.property.ThingProperty;
import city.sane.wot.thing.schema.NumberSchema;
import city.sane.wot.thing.schema.ObjectSchema;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.entity.ContentType;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import spark.Service;

import java.io.IOException;
import java.util.Date;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class ReadPropertyRouteIT {
    private static final ObjectMapper JSON_MAPPER = new ObjectMapper();
    private Service service;

    @BeforeEach
    public void setup() {
        service = Service.ignite().ipAddress("127.0.0.1").port(8080);
        service.defaultResponseTransformer(new ContentResponseTransformer());
        service.init();
        service.awaitInitialization();
        service.get(":id/properties/:name", new ReadPropertyRoute(null, null, Map.of("counter", getCounterThing())));
    }

    private ExposedThing getCounterThing() {
        ExposedThing thing = new ExposedThing(null)
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
    public void teardown() {
        service.stop();
        service.awaitStop();
    }

    @Test
    public void readProperty() throws IOException {
        HttpUriRequest request = new HttpGet("http://localhost:8080/counter/properties/lastChange");
        HttpResponse response = HttpClientBuilder.create().build().execute(request);

        assertEquals(200, response.getStatusLine().getStatusCode());
        assertEquals("application/json", ContentType.getOrDefault(response.getEntity()).getMimeType());
        assertEquals("2019-07-25 00:35:36", JSON_MAPPER.readValue(response.getEntity().getContent(), String.class));
    }

    @Test
    public void readPropertyWithCustomContentType() throws IOException {
        HttpUriRequest request = new HttpGet("http://localhost:8080/counter/properties/lastChange");
        request.addHeader("Content-Type", "text/plain");
        HttpResponse response = HttpClientBuilder.create().build().execute(request);

        assertEquals(200, response.getStatusLine().getStatusCode());
        assertEquals("text/plain", ContentType.getOrDefault(response.getEntity()).getMimeType());
        assertEquals("2019-07-25 00:35:36", EntityUtils.toString(response.getEntity()));
    }

    @Test
    public void readPropertyUnknownThing() throws IOException {
        HttpUriRequest request = new HttpGet("http://localhost:8080/zaehler/properties/lastChange");
        HttpResponse response = HttpClientBuilder.create().build().execute(request);

        assertEquals(404, response.getStatusLine().getStatusCode());
    }

    @Test
    public void readPropertyUnknownProperty() throws IOException {
        HttpUriRequest request = new HttpGet("http://localhost:8080/counter/properties/county");
        HttpResponse response = HttpClientBuilder.create().build().execute(request);

        assertEquals(404, response.getStatusLine().getStatusCode());
    }
}