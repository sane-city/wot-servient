package city.sane.wot.thing.event;

import city.sane.wot.Servient;
import city.sane.wot.ServientException;
import city.sane.wot.binding.ProtocolClient;
import city.sane.wot.binding.ProtocolClientException;
import city.sane.wot.binding.ProtocolClientFactory;
import city.sane.wot.binding.ProtocolClientNotImplementedException;
import city.sane.wot.content.Content;
import city.sane.wot.thing.ConsumedThing;
import city.sane.wot.thing.ConsumedThingException;
import city.sane.wot.thing.Thing;
import city.sane.wot.thing.form.Form;
import city.sane.wot.thing.form.Operation;
import city.sane.wot.thing.observer.Observer;
import city.sane.wot.thing.observer.Subscription;
import city.sane.wot.thing.schema.NumberSchema;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import org.junit.Test;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import static org.junit.Assert.assertEquals;

public class ConsumedThingEventTest {
    @Test
    public void subscribe() throws ServientException, ConsumedThingException, ExecutionException, InterruptedException {
        Config config = ConfigFactory
                .parseString("wot.servient.client-factories = [\"" + ConsumedThingEventTest.MyProtocolClientFactory.class.getName() + "\"]")
                .withFallback(ConfigFactory.load());
        Servient servient = new Servient(config);

        Thing thing = new Thing.Builder().build();
        ConsumedThing consumedThing = new ConsumedThing(servient, thing);

        Form form = new Form.Builder().setOp(Operation.SUBSCRIBE_EVENT).setHref("test:/myEvent").build();
        ThingEvent event = new ThingEvent.Builder().addForm(form).setData(new NumberSchema()).build();
        ConsumedThingEvent consumedThingEvent = new ConsumedThingEvent("myEvent", event, consumedThing);

        CompletableFuture<Object> future = new CompletableFuture<>();

        consumedThingEvent.subscribe(data -> future.complete(data)).get();

        assertEquals(1337, future.get());
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
            return new ConsumedThingEventTest.MyProtocolClient();
        }
    }

    static class MyProtocolClient implements ProtocolClient {
        @Override
        public CompletableFuture<Subscription> subscribeResource(Form form, Observer<Content> observer) throws ProtocolClientNotImplementedException {
            String json = null;
            switch (form.getHref()) {
                case "test:/myEvent":
                    json = "1337";
                    break;
            }
            observer.next(new Content("application/json", json.getBytes()));
            Subscription subscription = new Subscription();
            return CompletableFuture.completedFuture(subscription);
        }
    }
}