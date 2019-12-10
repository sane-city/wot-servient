package city.sane.wot.thing;

import city.sane.Pair;
import city.sane.wot.Servient;
import city.sane.wot.binding.ProtocolClient;
import city.sane.wot.binding.ProtocolClientException;
import city.sane.wot.binding.ProtocolClientFactory;
import city.sane.wot.content.Content;
import city.sane.wot.thing.form.Form;
import city.sane.wot.thing.form.Operation;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertEquals;

public class ConsumedThingTest {
    private ConsumedThing consumedThing;

    @Before
    public void setUp() throws Exception {
        Config config = ConfigFactory
                .parseString("wot.servient.client-factories = [\"" + ConsumedThingTest.MyProtocolClientFactory.class.getName() + "\"]")
                .withFallback(ConfigFactory.load());
        Servient servient = new Servient(config);
        Thing thing = new Thing.Builder()
                .setId("counter")
                .setTitle("Counter")
                .setForms(Collections.singletonList(new Form.Builder()
                        .setHref("test:/all")
                        .setOp(Operation.READ_ALL_PROPERTIES)
                        .build()
                ))
                .build();
        consumedThing = new ConsumedThing(servient, thing);
    }

    @After
    public void tearDown() throws Exception {
    }

    @Test
    public void getClientFor() throws ConsumedThingException {
        Form form = new Form.Builder().setHref("test:/count").setOp(Operation.READ_PROPERTY).build();
        Operation op = Operation.READ_PROPERTY;

        assertThat(consumedThing.getClientFor(Collections.singletonList(form), op), instanceOf(Pair.class));
    }

    @Test(expected = NoFormForInteractionConsumedThingException.class)
    public void getClientForWithUnsupportedOperation() throws ConsumedThingException {
        Form form = new Form.Builder().setHref("test:/count").setOp(Operation.READ_PROPERTY).build();
        Operation op = Operation.WRITE_PROPERTY;

        consumedThing.getClientFor(Collections.singletonList(form), op);
    }

    @Test(expected = NoClientFactoryForSchemesConsumedThingException.class)
    public void getClientForWithUnsupportedProtocol() throws ConsumedThingException {
        Form form = new Form.Builder().setHref("http:/count").setOp(Operation.READ_PROPERTY).build();
        Operation op = Operation.WRITE_PROPERTY;

        consumedThing.getClientFor(Collections.singletonList(form), op);
    }

    @Test
    public void readAllProperties() throws ExecutionException, InterruptedException {
        assertThat(consumedThing.readProperties().get(), instanceOf(Map.class));
    }

    @Test
    public void readSomeProperties() throws ExecutionException, InterruptedException {
        List<String> names = Collections.singletonList("count");
        assertThat(consumedThing.readProperties(names).get(), instanceOf(Map.class));
    }

    @Test
    public void handleUriVariables() {
        Form form = new Form.Builder().setHref("http://192.168.178.24:8080/counter/actions/increment{?step}").build();
        Map<String, Object> parameters = Map.of("step", 3);

        Form result = ConsumedThing.handleUriVariables(form, parameters);

        assertEquals("http://192.168.178.24:8080/counter/actions/increment?step=3", result.getHref());
    }

    @Test
    public void handleUriVariablesMultiple() {
        Form form = new Form.Builder().setHref("http://192.168.178.24:8080/counter/actions/increment{?step,direction}").build();
        Map<String, Object> parameters = Map.of("step", 3, "direction", "up");

        Form result = ConsumedThing.handleUriVariables(form, parameters);

        assertEquals("http://192.168.178.24:8080/counter/actions/increment?step=3&direction=up", result.getHref());
    }

    @Test
    public void testEquals() {
        ConsumedThing thingA = new ConsumedThing(null, new Thing.Builder().setId("counter").build());
        ConsumedThing thingB = new ConsumedThing(null, new Thing.Builder().setId("counter").build());

        assertEquals(thingA, thingB);
    }

    public static class MyProtocolClientFactory implements ProtocolClientFactory {
        public MyProtocolClientFactory(Config config) {

        }

        @Override
        public String getScheme() {
            return "test";
        }

        @Override
        public ProtocolClient getClient() throws ProtocolClientException {
            return new MyProtocolClient();
        }
    }

    static class MyProtocolClient implements ProtocolClient {
        @Override
        public CompletableFuture<Content> readResource(Form form) {
            String json = null;
            switch (form.getHref()) {
                case "test:/all":
                    json = "{\"count\": 1337}";
                    break;
            }
            return CompletableFuture.completedFuture(new Content("application/json", json.getBytes()));
        }
    }
}