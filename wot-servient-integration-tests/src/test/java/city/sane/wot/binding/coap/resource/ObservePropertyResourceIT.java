package city.sane.wot.binding.coap.resource;

import city.sane.wot.binding.ProtocolServerException;
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
import org.eclipse.californium.core.CoapClient;
import org.eclipse.californium.core.CoapHandler;
import org.eclipse.californium.core.CoapResponse;
import org.eclipse.californium.core.CoapServer;
import org.eclipse.californium.core.coap.MediaTypeRegistry;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.Date;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static org.awaitility.Awaitility.await;
import static org.junit.Assert.assertEquals;

public class ObservePropertyResourceIT {
    private CoapServer server;
    private ExposedThing thing;
    private int port;

    @BeforeEach
    public void setup() {
        thing = getCounterThing();

        server = new CoapServer(0);
        server.add(new ObservePropertyResource("count", thing.getProperty("count")));
        server.start();
        port = server.getEndpoints().get(0).getAddress().getPort();
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
                .setUriVariables(Map.of(
                        "step", Map.of(
                                "type", "integer",
                                "minimum", 1,
                                "maximum", 250
                        )
                ))
                .build();

        ThingProperty lastChangeProperty = new ThingProperty.Builder()
                .setType("string")
                .setDescription("last change of counter value")
                .setObservable(true)
                .setReadOnly(true)
                .build();

        thing.addProperty("count", counterProperty, 42);
        thing.addProperty("lastChange", lastChangeProperty, new Date().toString());

        thing.addAction("increment", new ThingAction.Builder()
                .setInput(new ObjectSchema())
                .setOutput(new NumberSchema())
                .build(), (input, options) -> {
            return thing.getProperty("count").read().thenApply(value -> {
                int step;
                if ((input instanceof Map) && ((Map) input).containsKey("step")) {
                    step = (int) ((Map) input).get("step");
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

        thing.addAction("decrement", new ThingAction<>(), (input, options) -> {
            return thing.getProperty("count").read().thenApply(value -> {
                int newValue = ((Integer) value) - 1;
                thing.getProperty("count").write(newValue);
                thing.getProperty("lastChange").write(new Date().toString());
                thing.getEvent("change").emit();
                return newValue;
            });
        });

        thing.addAction("reset", new ThingAction<>(), (input, options) -> {
            return thing.getProperty("count").write(0).thenApply(value -> {
                thing.getProperty("lastChange").write(new Date().toString());
                thing.getEvent("change").emit();
                return 0;
            });
        });

        thing.addEvent("change", new ThingEvent<>());

        return thing;
    }

    @AfterEach
    public void teardown() {
        server.stop();
    }

    @Test
    public void observeProperty() throws ExecutionException, InterruptedException, ContentCodecException, TimeoutException {
        CompletableFuture<Content> result = new CompletableFuture<>();
        CoapClient client = new CoapClient("coap://localhost:" + port + "/observable");
        client.observe(new CoapHandler() {
            @Override
            public void onLoad(CoapResponse response) {
                client.shutdown();
                String type = MediaTypeRegistry.toString(response.getOptions().getContentFormat());
                byte[] body = response.getPayload();
                Content output = new Content(type, body);
                result.complete(output);
            }

            @Override
            public void onError() {
                client.shutdown();
                result.completeExceptionally(new ProtocolServerException("Error while observe '" + client.getURI() + "'"));
            }
        });
        ExposedThingProperty<Object> property = thing.getProperty("count");

        // wait until client has established subscription
        await().atMost(Duration.ofSeconds(10)).until(property.getState().getSubject()::hasObservers);

        // write new value
        property.write(1337).get();

        // wait until new value is received
        Content content = result.get(10, TimeUnit.SECONDS);

        Object newValue = ContentManager.contentToValue(content, property);

        assertEquals(1337, newValue);
    }
}