package city.sane.wot.binding.coap;

import city.sane.wot.Servient;
import city.sane.wot.binding.ProtocolServerException;
import city.sane.wot.thing.ExposedThing;
import city.sane.wot.thing.action.ExposedThingAction;
import city.sane.wot.thing.action.ThingAction;
import city.sane.wot.thing.content.Content;
import city.sane.wot.thing.content.ContentCodecException;
import city.sane.wot.thing.content.ContentManager;
import city.sane.wot.thing.event.ExposedThingEvent;
import city.sane.wot.thing.event.ThingEvent;
import city.sane.wot.thing.property.ExposedThingProperty;
import city.sane.wot.thing.property.ThingProperty;
import city.sane.wot.thing.schema.IntegerSchema;
import city.sane.wot.thing.schema.NumberSchema;
import city.sane.wot.thing.schema.ObjectSchema;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import org.eclipse.californium.core.CoapClient;
import org.eclipse.californium.core.CoapHandler;
import org.eclipse.californium.core.CoapResponse;
import org.eclipse.californium.core.coap.CoAP;
import org.eclipse.californium.core.coap.MediaTypeRegistry;
import org.eclipse.californium.core.coap.Request;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertEquals;

public class CoapProtocolServerTest {
    private Servient servient;

    @Before
    public void setup() {
        Config config = ConfigFactory
                .parseString("wot.servient.servers = [\"city.sane.wot.binding.coap.CoapProtocolServer\"]\n" +
                        "wot.servient.client-factories = [\"city.sane.wot.binding.coap.CoapProtocolClientFactory\"]")
                .withFallback(ConfigFactory.load());

        servient = new Servient(config);
        servient.start().join();
    }

    @After
    public void teardown() {
        servient.shutdown().join();
    }

    @Test
    public void getAllThings() throws ContentCodecException {
        ExposedThing thing = getCounterThing();
        servient.addThing(thing);
        servient.expose(thing.getId()).join();

        CoapClient client = new CoapClient("coap://localhost:5683");
        CoapResponse response = client.get();

        assertEquals(CoAP.ResponseCode.CONTENT, response.getCode());

        int responseContentType = response.getOptions().getContentFormat();
        assertEquals(MediaTypeRegistry.APPLICATION_JSON, responseContentType);

        Content content = new Content(MediaTypeRegistry.toString(responseContentType), response.getPayload());
        Object responseValue = ContentManager.contentToValue(content, new ObjectSchema());
        assertThat(responseValue, instanceOf(Map.class));
    }

    @Test
    public void getAllThingsWithCustomContentType() throws ContentCodecException {
        ExposedThing thing = getCounterThing();
        servient.addThing(thing);
        servient.expose(thing.getId()).join();

        CoapClient client = new CoapClient("coap://localhost:5683");
        Request request = new Request(CoAP.Code.GET);
        request.getOptions().setContentFormat(MediaTypeRegistry.APPLICATION_CBOR);
        CoapResponse response = client.advanced(request);

        assertEquals(CoAP.ResponseCode.CONTENT, response.getCode());

        int responseContentType = response.getOptions().getContentFormat();
        assertEquals(MediaTypeRegistry.APPLICATION_CBOR, response.getOptions().getContentFormat());

        Content content = new Content(MediaTypeRegistry.toString(responseContentType), response.getPayload());
        Object responseValue = ContentManager.contentToValue(content, new ObjectSchema());
        assertThat(responseValue, instanceOf(Map.class));
    }

    @Test
    public void getThing() {
        ExposedThing thing = getCounterThing();
        servient.addThing(thing);
        servient.expose(thing.getId()).join();

        CoapClient client = new CoapClient("coap://localhost:5683/counter");
        CoapResponse response = client.get();

        assertEquals(CoAP.ResponseCode.CONTENT, response.getCode());
        assertEquals(MediaTypeRegistry.APPLICATION_JSON, response.getOptions().getContentFormat());
    }

    @Test
    public void getThingWithCustomContentType() {
        ExposedThing thing = getCounterThing();
        servient.addThing(thing);
        servient.expose(thing.getId()).join();

        CoapClient client = new CoapClient("coap://localhost:5683/counter");
        Request request = new Request(CoAP.Code.GET);
        request.getOptions().setContentFormat(MediaTypeRegistry.APPLICATION_CBOR);
        CoapResponse response = client.advanced(request);

        assertEquals(CoAP.ResponseCode.CONTENT, response.getCode());
        assertEquals(MediaTypeRegistry.APPLICATION_CBOR, response.getOptions().getContentFormat());
    }

    @Test
    public void readProperty() throws ContentCodecException {
        ExposedThing thing = getCounterThing();
        servient.addThing(thing);
        servient.expose(thing.getId()).join();

        CoapClient client = new CoapClient("coap://localhost:5683/counter/properties/count");
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
        ExposedThing thing = getCounterThing();
        servient.addThing(thing);
        servient.expose(thing.getId()).join();

        CoapClient client = new CoapClient("coap://localhost:5683/counter/properties/count");
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
        ExposedThing thing = getCounterThing();
        servient.addThing(thing);
        servient.expose(thing.getId()).join();

        CoapClient client = new CoapClient("coap://localhost:5683/counter/properties/count");
        CoapResponse response = client.put("1337", MediaTypeRegistry.APPLICATION_JSON);

        assertEquals(MediaTypeRegistry.APPLICATION_JSON, response.getOptions().getContentFormat());
        assertEquals(CoAP.ResponseCode.CHANGED, response.getCode());
    }

    @Test
    public void writePropertyWithCustomContentType() {
        ExposedThing thing = getCounterThing();
        servient.addThing(thing);
        servient.expose(thing.getId()).join();

        CoapClient client = new CoapClient("coap://localhost:5683/counter/properties/count");
        Request request = new Request(CoAP.Code.PUT);
        request.setPayload("1337");
        request.getOptions().setContentFormat(MediaTypeRegistry.APPLICATION_CBOR);
        CoapResponse response = client.advanced(request);

        assertEquals(MediaTypeRegistry.APPLICATION_CBOR, response.getOptions().getContentFormat());
        assertEquals(CoAP.ResponseCode.CHANGED, response.getCode());
    }

    @Test(timeout = 20 * 1000)
    public void observeProperty() throws ExecutionException, InterruptedException, ContentCodecException {
        ExposedThing thing = getCounterThing();
        servient.addThing(thing);
        servient.expose(thing.getId()).join();

        CompletableFuture<Content> result = new CompletableFuture<>();
        CoapClient client = new CoapClient("coap://localhost:5683/counter/properties/count/observable");
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

        // wait until client establish subcription
        // TODO: This is error-prone. We need a client that notifies us when the observation is active.
        Thread.sleep(5 * 1000);

        // write new value
        ExposedThingProperty property = thing.getProperty("count");
        property.write(1337).get();

        // wait until new value is received
        Content content = result.get();

        Object newValue = ContentManager.contentToValue(content, property);

        assertEquals(1337, newValue);
    }

    @Test
    public void readAllProperties() {
        ExposedThing thing = getCounterThing();
        servient.addThing(thing);
        servient.expose(thing.getId()).join();

        CoapClient client = new CoapClient("coap://localhost:5683/counter/all/properties");
        Request request = new Request(CoAP.Code.GET);
        CoapResponse response = client.advanced(request);

        assertEquals(MediaTypeRegistry.APPLICATION_JSON, response.getOptions().getContentFormat());
        assertEquals(CoAP.ResponseCode.CONTENT, response.getCode());
    }

    @Test
    public void invokeAction() throws ContentCodecException {
        ExposedThing thing = getCounterThing();
        servient.addThing(thing);
        servient.expose(thing.getId()).join();

        CoapClient client = new CoapClient("coap://localhost:5683/counter/actions/increment");
        CoapResponse response = client.post("", MediaTypeRegistry.APPLICATION_JSON);

        assertEquals(CoAP.ResponseCode.CONTENT, response.getCode());

        int responseContentType = response.getOptions().getContentFormat();
        assertEquals(MediaTypeRegistry.APPLICATION_JSON, responseContentType);

        Content content = new Content(MediaTypeRegistry.toString(responseContentType), response.getPayload());
        Object responseValue = ContentManager.contentToValue(content, new IntegerSchema());
        assertThat(responseValue, instanceOf(Integer.class));

        assertEquals(43, responseValue);
    }

    @Test
    public void invokeActionWithCustomContentType() throws ContentCodecException {
        ExposedThing thing = getCounterThing();
        servient.addThing(thing);
        servient.expose(thing.getId()).join();

        CoapClient client = new CoapClient("coap://localhost:5683/counter/actions/increment");
        Request request = new Request(CoAP.Code.POST);
        request.getOptions().setContentFormat(MediaTypeRegistry.APPLICATION_CBOR);
        CoapResponse response = client.advanced(request);

        assertEquals(CoAP.ResponseCode.CONTENT, response.getCode());

        int responseContentType = response.getOptions().getContentFormat();
        assertEquals(MediaTypeRegistry.APPLICATION_CBOR, responseContentType);

        Content content = new Content(MediaTypeRegistry.toString(responseContentType), response.getPayload());
        Object responseValue = ContentManager.contentToValue(content, new IntegerSchema());
        assertThat(responseValue, instanceOf(Integer.class));

        assertEquals(43, responseValue);
    }

    @Test
    public void invokeActionWithParameters() throws ContentCodecException {
        ExposedThing thing = getCounterThing();
        servient.addThing(thing);
        servient.expose(thing.getId()).join();

        CoapClient client = new CoapClient("coap://localhost:5683/counter/actions/increment");
        ExposedThingAction action = thing.getAction("increment");
        Content inputContent = ContentManager.valueToContent(new HashMap<>() {{
            put("step", 3);
        }}, "application/json");
        CoapResponse response = client.post(inputContent.getBody(), MediaTypeRegistry.APPLICATION_JSON);

        assertEquals(CoAP.ResponseCode.CONTENT, response.getCode());

        int responseContentType = response.getOptions().getContentFormat();
        assertEquals(MediaTypeRegistry.APPLICATION_JSON, responseContentType);

        Content outputContent = new Content(MediaTypeRegistry.toString(responseContentType), response.getPayload());
        Object responseValue = ContentManager.contentToValue(outputContent, action.getOutput());
        assertThat(responseValue, instanceOf(Integer.class));

        assertEquals(45, responseValue);
    }

    @Test
    public void subscribeEvent() throws ExecutionException, InterruptedException {
        ExposedThing thing = getCounterThing();
        servient.addThing(thing);
        servient.expose(thing.getId()).join();

        CompletableFuture<Void> result = new CompletableFuture<>();
        CoapClient client = new CoapClient("coap://localhost:5683/counter/events/change");
        client.observe(new CoapHandler() {
            @Override
            public void onLoad(CoapResponse response) {
                client.shutdown();
                result.complete(null);
            }

            @Override
            public void onError() {
                client.shutdown();
                result.completeExceptionally(new ProtocolServerException("Error while observe '" + client.getURI() + "'"));
            }
        });

        // wait until client establish subcription
        // TODO: This is error-prone. We need a client that notifies us when the observation is active.
        Thread.sleep(5 * 1000);

        // emit event
        ExposedThingEvent event = thing.getEvent("change");
        event.emit().get();

        // future should complete within a few seconds
        result.get();
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
                .setUriVariables(new HashMap<>() {{
                    put("step", new HashMap<>() {{
                        put("type", "integer");
                        put("minium", 1);
                        put("maximum", 250);
                    }});
                }})
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
