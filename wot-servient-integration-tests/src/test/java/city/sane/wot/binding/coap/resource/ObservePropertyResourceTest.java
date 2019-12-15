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
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.Date;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import static org.junit.Assert.assertEquals;

public class ObservePropertyResourceTest {
    private CoapServer server;
    private ExposedThing thing;

    @Before
    public void setup() {
        thing = getCounterThing();

        server = new CoapServer(5683);
        server.add(new ObservePropertyResource("count", thing.getProperty("count")));
        server.start();
    }

    @After
    public void teardown() {
        server.stop();

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

    @Test(timeout = 20 * 1000)
    public void observeProperty() throws ExecutionException, InterruptedException, ContentCodecException {
        CompletableFuture<Content> result = new CompletableFuture<>();
        CoapClient client = new CoapClient("coap://localhost:5683/observable");
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

        // wait until client establish subscription
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