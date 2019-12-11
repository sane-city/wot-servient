package city.sane.wot.thing.action;

import city.sane.wot.Servient;
import city.sane.wot.ServientException;
import city.sane.wot.binding.ProtocolClient;
import city.sane.wot.binding.ProtocolClientFactory;
import city.sane.wot.content.Content;
import city.sane.wot.thing.ConsumedThing;
import city.sane.wot.thing.Thing;
import city.sane.wot.thing.form.Form;
import city.sane.wot.thing.form.Operation;
import city.sane.wot.thing.schema.NumberSchema;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import org.junit.Test;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import static org.junit.Assert.assertEquals;

public class ConsumedThingActionTest {
    @Test
    public void invoke() throws ExecutionException, InterruptedException, ServientException {
        Config config = ConfigFactory
                .parseString("wot.servient.client-factories = [\"" + MyProtocolClientFactory.class.getName() + "\"]")
                .withFallback(ConfigFactory.load());
        Servient servient = new Servient(config);

        Thing thing = new Thing.Builder().build();
        ConsumedThing consumedThing = new ConsumedThing(servient, thing);

        Form form = new Form.Builder().setOp(Operation.INVOKE_ACTION).setHref("test:/myAction").build();
        ThingAction action = new ThingAction.Builder().addForm(form).setOutput(new NumberSchema()).build();
        ConsumedThingAction consumedThingAction = new ConsumedThingAction("myAction", action, consumedThing);

        assertEquals(1337, consumedThingAction.invoke().get());
    }

    public static class MyProtocolClientFactory implements ProtocolClientFactory {
        @Override
        public String getScheme() {
            return "test";
        }

        @Override
        public ProtocolClient getClient() {
            return new ConsumedThingActionTest.MyProtocolClient();
        }
    }

    static class MyProtocolClient implements ProtocolClient {
        @Override
        public CompletableFuture<Content> invokeResource(Form form, Content content) {
            String json = null;
            if ("test:/myAction".equals(form.getHref())) {
                json = "1337";
            }
            return CompletableFuture.completedFuture(new Content("application/json", json.getBytes()));
        }
    }
}