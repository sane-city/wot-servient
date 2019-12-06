package city.sane.wot.examples;

import city.sane.wot.DefaultWot;
import city.sane.wot.Wot;
import city.sane.wot.WotException;
import city.sane.wot.thing.ConsumedThing;
import city.sane.wot.thing.ConsumedThingException;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

/**
 * Fetch thing description exposes by {@link ExampleEvent} and then subscribe to the event.
 */
public class ExampleEventClient {
    public static void main(String[] args) throws URISyntaxException, IOException, WotException {
        // create wot
        Wot wot = DefaultWot.clientOnly();

        URI url = new URI("coap://localhost:5683/EventSource");
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

                consumedThing.getEvent("onchange").subscribe(
                        next -> System.out.println("ExampleDynamicClient: next = " + next),
                        ex -> System.out.println("ExampleDynamicClient: error = " + ex.toString()),
                        () -> System.out.println("ExampleDynamicClient: completed!")
                );
                System.out.println("ExampleDynamicClient: Subscribed");

            }
            catch (ConsumedThingException ex) {
                throw new RuntimeException(ex);
            }
        }).join();

        System.out.println("Press ENTER to exit the client");
        System.in.read();
    }
}
