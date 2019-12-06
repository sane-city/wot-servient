package city.sane.wot.examples;

import city.sane.wot.DefaultWot;
import city.sane.wot.Wot;
import city.sane.wot.WotException;
import city.sane.wot.thing.ConsumedThing;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Map;
import java.util.concurrent.ExecutionException;

/**
 * Fetch thing description exposes by {@link CounterUriVariables} and then interact with it.
 */
public class CounterUriVariablesClient {
    public static void main(String[] args) throws URISyntaxException, WotException {
        // create wot
        Wot wot = DefaultWot.clientOnly();

        URI url = new URI("coap://localhost:5683/counter");
        wot.fetch(url).whenComplete((thing, e) -> {
            try {
                if (e != null) {
                    throw new RuntimeException(e);
                }

                System.out.println("=== TD ===");
                String json = thing.toJson(true);
                System.out.println(json);
                System.out.println("==========");

                ConsumedThing consumedThing = wot.consume(thing);

                // increment property #1 (without step)
                consumedThing.getAction("increment").invoke().join();
                Object inc1 = consumedThing.getProperty("count").read().get();
                System.out.println("CounterUriVariablesClient: count value after increment #1 is " + inc1);

                // increment property #2
                consumedThing.getAction("increment").invoke(Map.of("step", 3)).join();
                Object inc2 = consumedThing.getProperty("count").read().get();
                System.out.println("CounterUriVariablesClient: count value after increment #2 (with step 3) is " + inc2);

                // decrement property
                consumedThing.getAction("decrement").invoke().join();
                Object dec1 = consumedThing.getProperty("count").read().get();
                System.out.println("CounterUriVariablesClient: count value after decrement is " + dec1);

            }
            catch (InterruptedException ex) {
                Thread.currentThread().interrupt();
            }
            catch (ExecutionException ex) {
                throw new RuntimeException(ex);
            }
        }).join();
    }
}
