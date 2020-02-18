package city.sane.wot.binding.http.route;

import city.sane.wot.binding.http.ContentResponseTransformer;
import city.sane.wot.thing.ExposedThing;
import city.sane.wot.thing.action.ThingAction;
import city.sane.wot.thing.event.ThingEvent;
import city.sane.wot.thing.property.ThingProperty;
import city.sane.wot.thing.schema.NumberSchema;
import city.sane.wot.thing.schema.ObjectSchema;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClientBuilder;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import spark.Service;

import java.io.IOException;
import java.util.Date;
import java.util.Map;

import static org.junit.Assert.assertEquals;

public class WritePropertyRouteIT {
    private Service service;

    @Before
    public void setup() {
        service = Service.ignite().ipAddress("127.0.0.1").port(8080);
        service.defaultResponseTransformer(new ContentResponseTransformer());
        service.init();
        service.awaitInitialization();
        service.put(":id/properties/:name", new WritePropertyRoute(null, null, Map.of("counter", getCounterThing())));
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

    @After
    public void teardown() {
        service.stop();
        service.awaitStop();
    }

    @Test
    public void writeProperty() throws IOException {
        HttpPut request = new HttpPut("http://localhost:8080/counter/properties/count");
        request.setEntity(new StringEntity("1337"));
        HttpResponse response = HttpClientBuilder.create().build().execute(request);

        assertEquals(204, response.getStatusLine().getStatusCode());
        assertEquals("text/plain", ContentType.getOrDefault(response.getEntity()).getMimeType());
    }

    @Test
    public void writePropertyUnknownThing() throws IOException {
        HttpPut request = new HttpPut("http://localhost:8080/zaehler/properties/count");
        request.setEntity(new StringEntity("1337"));
        HttpResponse response = HttpClientBuilder.create().build().execute(request);

        assertEquals(404, response.getStatusLine().getStatusCode());
    }

    @Test
    public void writePropertyUnknownProperty() throws IOException {
        HttpPut request = new HttpPut("http://localhost:8080/counter/properties/county");
        request.setEntity(new StringEntity("1337"));
        HttpResponse response = HttpClientBuilder.create().build().execute(request);

        assertEquals(404, response.getStatusLine().getStatusCode());
    }

    @Test
    public void writePropertyReadOnly() throws IOException {
        HttpPut request = new HttpPut("http://localhost:8080/counter/properties/lastChange");
        request.setEntity(new StringEntity("1337"));
        HttpResponse response = HttpClientBuilder.create().build().execute(request);

        assertEquals(400, response.getStatusLine().getStatusCode());
    }
}