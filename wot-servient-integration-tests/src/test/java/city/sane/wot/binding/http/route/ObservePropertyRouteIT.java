package city.sane.wot.binding.http.route;

import city.sane.wot.binding.http.ContentResponseTransformer;
import city.sane.wot.content.Content;
import city.sane.wot.content.ContentCodecException;
import city.sane.wot.content.ContentManager;
import city.sane.wot.thing.ExposedThing;
import city.sane.wot.thing.action.ThingAction;
import city.sane.wot.thing.event.ThingEvent;
import city.sane.wot.thing.property.ExposedThingProperty;
import city.sane.wot.thing.property.ThingProperty;
import city.sane.wot.thing.schema.NumberSchema;
import city.sane.wot.thing.schema.ObjectSchema;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.impl.client.HttpClientBuilder;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import spark.Service;

import java.io.IOException;
import java.time.Duration;
import java.util.Date;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.ExecutionException;

import static java.util.concurrent.CompletableFuture.supplyAsync;
import static org.awaitility.Awaitility.await;
import static org.junit.Assert.assertEquals;

public class ObservePropertyRouteIT {
    private Service service;
    private ExposedThing thing;

    @BeforeEach
    public void setup() {
        thing = getCounterThing();

        service = Service.ignite().ipAddress("127.0.0.1").port(8080);
        service.defaultResponseTransformer(new ContentResponseTransformer());
        service.init();
        service.awaitInitialization();
        service.get(":id/properties/:name/observable", new ObservePropertyRoute(null, null, Map.of("counter", thing)));
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
    public void observeProperty() throws InterruptedException, ExecutionException, ContentCodecException {
        CompletableFuture<Content> result = supplyAsync(() -> {
            try {
                HttpUriRequest request = new HttpGet("http://localhost:8080/counter/properties/count/observable");
                HttpResponse response = HttpClientBuilder.create().build().execute(request);
                byte[] body = response.getEntity().getContent().readAllBytes();
                Content content = new Content("application/json", body);
                return content;
            }
            catch (IOException e) {
                throw new CompletionException(e);
            }
        });
        ExposedThingProperty<Object> property = thing.getProperty("count");

        // wait until client has established subscription
        await().atMost(Duration.ofSeconds(10)).until(property.getState().getSubject()::hasObservers);

        // write new value
        property.write(1337).get();

        // wait until new value is received
        Content content = result.get();

        Object newValue = ContentManager.contentToValue(content, property);

        assertEquals(1337, newValue);
    }
}