package city.sane.wot.binding.coap.resource;

import city.sane.wot.content.Content;
import city.sane.wot.content.ContentCodecException;
import city.sane.wot.content.ContentManager;
import city.sane.wot.thing.ExposedThing;
import city.sane.wot.thing.action.ThingAction;
import city.sane.wot.thing.event.ThingEvent;
import city.sane.wot.thing.property.ThingProperty;
import city.sane.wot.thing.schema.IntegerSchema;
import city.sane.wot.thing.schema.NumberSchema;
import city.sane.wot.thing.schema.ObjectSchema;
import org.eclipse.californium.core.CoapClient;
import org.eclipse.californium.core.CoapResponse;
import org.eclipse.californium.core.CoapServer;
import org.eclipse.californium.core.coap.CoAP;
import org.eclipse.californium.core.coap.MediaTypeRegistry;
import org.eclipse.californium.core.coap.Request;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Date;
import java.util.Map;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class PropertyResourceIT {
    private CoapServer server;
    private int port;

    @BeforeEach
    public void setup() {
        ExposedThing thing = getCounterThing();

        server = new CoapServer(0);
        server.add(new PropertyResource("count", thing.getProperty("count")));
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
    public void readProperty() throws ContentCodecException {
        CoapClient client = new CoapClient("coap://localhost:" + port + "/count");
        CoapResponse response = client.get();

        int responseContentType = response.getOptions().getContentFormat();
        assertEquals(MediaTypeRegistry.APPLICATION_JSON, responseContentType);

        Content content = new Content(MediaTypeRegistry.toString(responseContentType), response.getPayload());
        Object responseValue = ContentManager.contentToValue(content, new IntegerSchema());
        assertThat(responseValue, instanceOf(Integer.class));

        assertEquals(42, responseValue);
    }

    @Test
    public void readPropertyWithCustomContentType() throws ContentCodecException {
        CoapClient client = new CoapClient("coap://localhost:" + port + "/count");
        Request request = new Request(CoAP.Code.GET);
        request.getOptions().setContentFormat(MediaTypeRegistry.APPLICATION_CBOR);
        CoapResponse response = client.advanced(request);

        assertEquals(CoAP.ResponseCode.CONTENT, response.getCode());

        int responseContentType = response.getOptions().getContentFormat();
        assertEquals(MediaTypeRegistry.APPLICATION_CBOR, responseContentType);

        Content content = new Content(MediaTypeRegistry.toString(responseContentType), response.getPayload());
        Object responseValue = ContentManager.contentToValue(content, new IntegerSchema());
        assertThat(responseValue, instanceOf(Integer.class));

        assertEquals(42, responseValue);
    }

    @Test
    public void writeProperty() {
        CoapClient client = new CoapClient("coap://localhost:" + port + "/count");
        CoapResponse response = client.put("1337", MediaTypeRegistry.APPLICATION_JSON);

        assertEquals(MediaTypeRegistry.APPLICATION_JSON, response.getOptions().getContentFormat());
        assertEquals(CoAP.ResponseCode.CHANGED, response.getCode());
    }

    @Test
    public void writePropertyWithCustomContentType() {
        CoapClient client = new CoapClient("coap://localhost:" + port + "/count");
        Request request = new Request(CoAP.Code.PUT);
        request.setPayload("1337");
        request.getOptions().setContentFormat(MediaTypeRegistry.APPLICATION_CBOR);
        CoapResponse response = client.advanced(request);

        assertEquals(MediaTypeRegistry.APPLICATION_CBOR, response.getOptions().getContentFormat());
        assertEquals(CoAP.ResponseCode.CHANGED, response.getCode());
    }
}