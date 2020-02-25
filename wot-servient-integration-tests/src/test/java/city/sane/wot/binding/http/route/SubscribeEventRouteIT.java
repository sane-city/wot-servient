package city.sane.wot.binding.http.route;

import city.sane.wot.binding.http.ContentResponseTransformer;
import city.sane.wot.content.Content;
import city.sane.wot.thing.ExposedThing;
import city.sane.wot.thing.action.ThingAction;
import city.sane.wot.thing.event.ExposedThingEvent;
import city.sane.wot.thing.event.ThingEvent;
import city.sane.wot.thing.property.ThingProperty;
import city.sane.wot.thing.schema.NumberSchema;
import city.sane.wot.thing.schema.ObjectSchema;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.impl.client.HttpClientBuilder;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import spark.Service;

import java.io.IOException;
import java.util.Date;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import static java.util.concurrent.CompletableFuture.runAsync;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;

public class SubscribeEventRouteIT {
    private Service service;
    private ExposedThing thing;

    @Before
    public void setup() {
        thing = getCounterThing();

        service = Service.ignite().ipAddress("127.0.0.1").port(8080);
        service.defaultResponseTransformer(new ContentResponseTransformer());
        service.init();
        service.awaitInitialization();
        service.get(":id/events/:name", new SubscribeEventRoute(null, null, Map.of("counter", thing)));
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

    @After
    public void teardown() {
        service.stop();
        service.awaitStop();
    }

    @Test
    public void shouldReturnValueOnEventEmit() throws InterruptedException, ExecutionException {
        CompletableFuture<Content> result = new CompletableFuture<>();
        runAsync(() -> {
            try {
                HttpUriRequest request = new HttpGet("http://localhost:8080/counter/events/change");
                HttpResponse response = HttpClientBuilder.create().build().execute(request);
                byte[] body = response.getEntity().getContent().readAllBytes();
                Content content = new Content("application/json", body);
                result.complete(content);
            }
            catch (IOException e) {
                result.completeExceptionally(e);
            }
        });

        // wait until client establish subscription
        // TODO: This is error-prone. We need a client that notifies us when the observation is active.
        Thread.sleep(5 * 1000L);

        // emit event
        ExposedThingEvent<Object> event = thing.getEvent("change");
        event.emit();

        // future should complete within a few seconds
        result.get();

        assertThat(result.get(), instanceOf(Content.class));
    }

    @Test
    public void subscribeEventObserverPopulation() throws InterruptedException, ExecutionException {
        runAsync(() -> {
            try {
                HttpUriRequest request = new HttpGet("http://localhost:8080/counter/events/change");
                HttpClientBuilder.create().build().execute(request);
            }
            catch (IOException e) {
                throw new RuntimeException(e);
            }
        });

        // wait until client establish subscription
        // TODO: This is error-prone. We need a client that notifies us when the observation is active.
        Thread.sleep(5 * 1000L);

        // emit event
        ExposedThingEvent<Object> event = thing.getEvent("change");
        event.emit();

        // wait until client received answer
        // TODO: This is error-prone. We need a client that notifies us when the observation is active.
        Thread.sleep(5 * 1000L);

        assertFalse("populated observer should have been removed", thing.getEvent("change").getState().getSubject().hasObservers());
    }
}