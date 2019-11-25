package city.sane.wot.binding.http;

import city.sane.wot.Servient;
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
import com.fasterxml.jackson.databind.ObjectMapper;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class HttpProtocolServerTest {
    private static final ObjectMapper JSON_MAPPER = new ObjectMapper();

    private Servient servient;

    @Before
    public void setup() {
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
    public void getAllThings() throws IOException {
        ExposedThing thing = getCounterThing();
        servient.addThing(thing);
        servient.expose(thing.getId()).join();

        HttpUriRequest request = new HttpGet("http://localhost:8080/things");
        HttpResponse response = HttpClientBuilder.create().build().execute(request);

        assertEquals(200, response.getStatusLine().getStatusCode());
        assertEquals("application/json", ContentType.getOrDefault(response.getEntity()).getMimeType());
        assertTrue(
                "Should return map with \"counter\" element",
                JSON_MAPPER.readValue(response.getEntity().getContent(), Map.class).containsKey("counter")
        );
    }

    @Test
    public void getThing() throws IOException {
        ExposedThing thing = getCounterThing();
        servient.addThing(thing);
        servient.expose(thing.getId()).join();

        HttpUriRequest request = new HttpGet("http://localhost:8080/things/counter");
        HttpResponse response = HttpClientBuilder.create().build().execute(request);

        assertEquals(200, response.getStatusLine().getStatusCode());
        assertEquals("application/json", ContentType.getOrDefault(response.getEntity()).getMimeType());
        assertTrue(
                "Should return map with \"id\" element",
                JSON_MAPPER.readValue(response.getEntity().getContent(), Map.class).containsKey("id")
        );
    }

    @Test
    public void readProperty() throws IOException {
        ExposedThing thing = getCounterThing();
        servient.addThing(thing);
        servient.expose(thing.getId()).join();

        HttpUriRequest request = new HttpGet("http://localhost:8080/things/counter/properties/lastChange");
        HttpResponse response = HttpClientBuilder.create().build().execute(request);

        assertEquals(200, response.getStatusLine().getStatusCode());
        assertEquals("application/json", ContentType.getOrDefault(response.getEntity()).getMimeType());
        assertEquals("2019-07-25 00:35:36", JSON_MAPPER.readValue(response.getEntity().getContent(), String.class));
    }

    @Test
    public void readPropertyWithCustomContentType() throws IOException {
        ExposedThing thing = getCounterThing();
        servient.addThing(thing);
        servient.expose(thing.getId()).join();

        HttpUriRequest request = new HttpGet("http://localhost:8080/things/counter/properties/lastChange");
        request.addHeader("Content-Type", "text/plain");
        HttpResponse response = HttpClientBuilder.create().build().execute(request);

        assertEquals(200, response.getStatusLine().getStatusCode());
        assertEquals("text/plain", ContentType.getOrDefault(response.getEntity()).getMimeType());
        assertEquals("2019-07-25 00:35:36", EntityUtils.toString(response.getEntity()));
    }

    @Test
    public void writeProperty() throws IOException {
        ExposedThing thing = getCounterThing();
        servient.addThing(thing);
        servient.expose(thing.getId()).join();

        HttpPut request = new HttpPut("http://localhost:8080/things/counter/properties/count");
        request.setEntity(new StringEntity("1337"));
        HttpResponse response = HttpClientBuilder.create().build().execute(request);

        assertEquals(204, response.getStatusLine().getStatusCode());
        assertEquals("text/plain", ContentType.getOrDefault(response.getEntity()).getMimeType());
    }

    @Test
    public void observeProperty() throws InterruptedException, ExecutionException, ContentCodecException {
        ExposedThing thing = getCounterThing();
        servient.addThing(thing);
        servient.expose(thing.getId()).join();

        CompletableFuture<Content> result = new CompletableFuture<>();
        CompletableFuture.runAsync(() -> {
            try {
                HttpUriRequest request = new HttpGet("http://localhost:8080/things/counter/properties/count/observable");
                HttpResponse response = HttpClientBuilder.create().build().execute(request);
                byte[] body = response.getEntity().getContent().readAllBytes();
                Content content = new Content("application/json", body);
                result.complete(content);
            } catch (IOException e) {
                result.completeExceptionally(e);
            }
        });

        // wait until client establish subcription
        // TODO: This is error-prone. We need a client that notifies us when the observation is active.
        Thread.sleep(5 * 1000L);

        // write new value
        ExposedThingProperty property = thing.getProperty("count");
        property.write(1337).get();

        // wait until new value is received
        Content content = result.get();

        Object newValue = ContentManager.contentToValue(content, property);

        assertEquals(1337, newValue);
    }

    @Test
    public void readAllProperties() throws IOException {
        ExposedThing thing = getCounterThing();
        servient.addThing(thing);
        servient.expose(thing.getId()).join();

        HttpUriRequest request = new HttpGet("http://localhost:8080/things/counter/all/properties");
        HttpResponse response = HttpClientBuilder.create().build().execute(request);

        assertEquals(200, response.getStatusLine().getStatusCode());
        assertEquals("application/json", ContentType.getOrDefault(response.getEntity()).getMimeType());
        assertTrue(
                "Should return map with \"count\" element",
                JSON_MAPPER.readValue(response.getEntity().getContent(), Map.class).containsKey("count")
        );
    }

    @Test
    public void invokeAction() throws IOException {
        ExposedThing thing = getCounterThing();
        servient.addThing(thing);
        servient.expose(thing.getId()).join();

        HttpUriRequest request = new HttpPost("http://localhost:8080/things/counter/actions/increment");
        HttpResponse response = HttpClientBuilder.create().build().execute(request);

        assertEquals(200, response.getStatusLine().getStatusCode());
        assertEquals("application/json", ContentType.getOrDefault(response.getEntity()).getMimeType());
        assertEquals("43", EntityUtils.toString(response.getEntity()));
    }

    @Test
    public void invokeActionWithUrlParameters() throws IOException {
        ExposedThing thing = getCounterThing();
        servient.addThing(thing);
        servient.expose(thing.getId()).join();

        HttpUriRequest request = new HttpPost("http://localhost:8080/things/counter/actions/increment?step=3");
        HttpResponse response = HttpClientBuilder.create().build().execute(request);

        assertEquals(200, response.getStatusLine().getStatusCode());
        assertEquals("application/json", ContentType.getOrDefault(response.getEntity()).getMimeType());
        assertEquals("45", EntityUtils.toString(response.getEntity()));
    }

    @Test
    public void invokeActionWithEntityParameters() throws IOException, ContentCodecException {
        ExposedThing thing = getCounterThing();
        servient.addThing(thing);
        servient.expose(thing.getId()).join();

        HashMap<Object, Object> parameters = new HashMap<>() {{
            put("step", 3);
        }};
        Content content = ContentManager.valueToContent(parameters, "application/json");

        HttpPost request = new HttpPost("http://localhost:8080/things/counter/actions/increment");
        request.setHeader("Content-Type", content.getType());
        request.setEntity(new ByteArrayEntity(content.getBody()));
        HttpResponse response = HttpClientBuilder.create().build().execute(request);

        assertEquals(200, response.getStatusLine().getStatusCode());
        assertEquals("application/json", ContentType.getOrDefault(response.getEntity()).getMimeType());
        assertEquals("45", EntityUtils.toString(response.getEntity()));
    }

    @Test
    public void subscribeEvent() throws InterruptedException, ExecutionException {
        ExposedThing thing = getCounterThing();
        servient.addThing(thing);
        servient.expose(thing.getId()).join();

        CompletableFuture<Content> result = new CompletableFuture<>();
        CompletableFuture.runAsync(() -> {
            try {
                HttpUriRequest request = new HttpGet("http://localhost:8080/things/counter/events/change");
                HttpResponse response = HttpClientBuilder.create().build().execute(request);
                byte[] body = response.getEntity().getContent().readAllBytes();
                Content content = new Content("application/json", body);
                result.complete(content);
            } catch (IOException e) {
                result.completeExceptionally(e);
            }
        });

        // wait until client establish subcription
        // TODO: This is error-prone. We need a client that notifies us when the observation is active.
        Thread.sleep(5 * 1000L);

        // emit event
        ExposedThingEvent event = thing.getEvent("change");
        event.emit().get();

        // future should complete within a few seconds
        result.get();
    }

    @Test
    public void subscribeEventObserverPopulation() throws InterruptedException, ExecutionException {
        ExposedThing thing = getCounterThing();
        servient.addThing(thing);
        servient.expose(thing.getId()).join();

        CompletableFuture.runAsync(() -> {
            try {
                HttpUriRequest request = new HttpGet("http://localhost:8080/things/counter/events/change");
                HttpClientBuilder.create().build().execute(request);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });

        // wait until client establish subcription
        // TODO: This is error-prone. We need a client that notifies us when the observation is active.
        Thread.sleep(5 * 1000L);

        // emit event
        ExposedThingEvent event = thing.getEvent("change");
        event.emit().get();

        // wait until client received answer
        // TODO: This is error-prone. We need a client that notifies us when the observation is active.
        Thread.sleep(5 * 1000L);

        assertTrue("populated observer should have been removed", thing.getEvent("change").getState().getSubject().getObservers().isEmpty());
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
                .setUriVariables(new HashMap<>() {{
                    put("step", new HashMap<>() {{
                        put("type", "integer");
                        put("minium", 1);
                        put("maximum", 250);
                    }});
                }})
                .build(), (input, options) -> {
            return thing.getProperty("count").read().thenApply(value -> {
                int step;
                if (input != null && ((Map) input).containsKey("step")) {
                    step = (Integer) ((Map) input).get("step");
                } else if (options.containsKey("uriVariables") && ((Map) options.get("uriVariables")).containsKey("step")) {
                    step = (int) ((Map) options.get("uriVariables")).get("step");
                } else {
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