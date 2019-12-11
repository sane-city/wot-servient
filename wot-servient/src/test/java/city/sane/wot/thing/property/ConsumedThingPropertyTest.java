package city.sane.wot.thing.property;

import city.sane.wot.Servient;
import city.sane.wot.ServientException;
import city.sane.wot.binding.ProtocolClient;
import city.sane.wot.binding.ProtocolClientFactory;
import city.sane.wot.content.Content;
import city.sane.wot.thing.ConsumedThing;
import city.sane.wot.thing.ConsumedThingException;
import city.sane.wot.thing.Thing;
import city.sane.wot.thing.form.Form;
import city.sane.wot.thing.form.Operation;
import city.sane.wot.thing.observer.Observer;
import city.sane.wot.thing.observer.Subscription;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import org.junit.Test;

import java.util.Collections;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import static org.junit.Assert.assertEquals;

public class ConsumedThingPropertyTest {
    @Test
    public void normalizeAbsoluteHref() {
        Thing thing = new Thing.Builder().build();
        ConsumedThing consumedThing = new ConsumedThing(null, thing);
        Form form = new Form.Builder().setHref("http://example.com/properties/count").build();
        ThingProperty property = new ThingProperty.Builder().setForms(Collections.singletonList(form)).build();
        ConsumedThingProperty consumedProperty = new ConsumedThingProperty("count", property, consumedThing);

        assertEquals("http://example.com/properties/count", consumedProperty.getForms().get(0).getHref());
    }

    @Test
    public void normalizeRelativeHref() {
        Thing thing = new Thing.Builder().setBase("http://example.com").build();
        ConsumedThing consumedThing = new ConsumedThing(null, thing);
        Form form = new Form.Builder().setHref("/properties/count").build();
        ThingProperty property = new ThingProperty.Builder().setForms(Collections.singletonList(form)).build();
        ConsumedThingProperty consumedProperty = new ConsumedThingProperty("count", property, consumedThing);

        assertEquals("http://example.com/properties/count", consumedProperty.getForms().get(0).getHref());
    }

    @Test
    public void normalizeAbsoluteHrefWithBase() {
        Thing thing = new Thing.Builder().setBase("http://example.com").build();
        ConsumedThing consumedThing = new ConsumedThing(null, thing);
        Form form = new Form.Builder().setHref("http://example.com/properties/count").build();
        ThingProperty property = new ThingProperty.Builder().setForms(Collections.singletonList(form)).build();
        ConsumedThingProperty consumedProperty = new ConsumedThingProperty("count", property, consumedThing);

        assertEquals("http://example.com/properties/count", consumedProperty.getForms().get(0).getHref());
    }

    @Test
    public void read() throws ServientException, ExecutionException, InterruptedException {
        Config config = ConfigFactory
                .parseString("wot.servient.client-factories = [\"" + ConsumedThingPropertyTest.MyProtocolClientFactory.class.getName() + "\"]")
                .withFallback(ConfigFactory.load());
        Servient servient = new Servient(config);

        Thing thing = new Thing.Builder().build();
        ConsumedThing consumedThing = new ConsumedThing(servient, thing);

        Form form = new Form.Builder().setOp(Operation.READ_PROPERTY).setHref("test:/count/read").build();
        ThingProperty property = new ThingProperty.Builder().addForm(form).build();
        ConsumedThingProperty consumedThingProperty = new ConsumedThingProperty("myProperty", property, consumedThing);

        assertEquals(1337, consumedThingProperty.read().get());
    }

    @Test
    public void write() throws ExecutionException, InterruptedException, ServientException {
        Config config = ConfigFactory
                .parseString("wot.servient.client-factories = [\"" + ConsumedThingPropertyTest.MyProtocolClientFactory.class.getName() + "\"]")
                .withFallback(ConfigFactory.load());
        Servient servient = new Servient(config);

        Thing thing = new Thing.Builder().build();
        ConsumedThing consumedThing = new ConsumedThing(servient, thing);

        Form form = new Form.Builder().setOp(Operation.WRITE_PROPERTY).setHref("test:/count/write").build();
        ThingProperty property = new ThingProperty.Builder().addForm(form).build();
        ConsumedThingProperty consumedThingProperty = new ConsumedThingProperty("myProperty", property, consumedThing);

        assertEquals(true, consumedThingProperty.write(1337).get());
    }

    @Test
    public void subscribe() throws ConsumedThingException, ExecutionException, InterruptedException, ServientException {
        Config config = ConfigFactory
                .parseString("wot.servient.client-factories = [\"" + ConsumedThingPropertyTest.MyProtocolClientFactory.class.getName() + "\"]")
                .withFallback(ConfigFactory.load());
        Servient servient = new Servient(config);

        Thing thing = new Thing.Builder().build();
        ConsumedThing consumedThing = new ConsumedThing(servient, thing);

        Form form = new Form.Builder().setOp(Operation.OBSERVE_PROPERTY).setHref("test:/count/observe").build();
        ThingProperty property = new ThingProperty.Builder().addForm(form).build();
        ConsumedThingProperty consumedThingProperty = new ConsumedThingProperty("myProperty", property, consumedThing);

        CompletableFuture<Object> future = new CompletableFuture<>();

        consumedThingProperty.subscribe(future::complete).get();

        assertEquals(42, future.get());
    }

    public static class MyProtocolClientFactory implements ProtocolClientFactory {
        @Override
        public String getScheme() {
            return "test";
        }

        @Override
        public ProtocolClient getClient() {
            return new ConsumedThingPropertyTest.MyProtocolClient();
        }
    }

    static class MyProtocolClient implements ProtocolClient {
        @Override
        public CompletableFuture<Content> readResource(Form form) {
            String json = null;
            if ("test:/count/read".equals(form.getHref())) {
                json = "1337";
            }
            return CompletableFuture.completedFuture(new Content("application/json", json.getBytes()));
        }

        @Override
        public CompletableFuture<Content> writeResource(Form form, Content content) {
            String json = null;
            if ("test:/count/write".equals(form.getHref())) {
                json = "true";
            }
            return CompletableFuture.completedFuture(new Content("application/json", json.getBytes()));
        }

        @Override
        public CompletableFuture<Subscription> subscribeResource(Form form, Observer<Content> observer) {
            String json = null;
            if ("test:/count/observe".equals(form.getHref())) {
                json = "42";
            }
            observer.next(new Content("application/json", json.getBytes()));
            Subscription subscription = new Subscription();
            return CompletableFuture.completedFuture(subscription);
        }
    }
}