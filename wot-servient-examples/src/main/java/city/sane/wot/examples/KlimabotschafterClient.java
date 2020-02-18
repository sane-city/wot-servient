package city.sane.wot.examples;

import city.sane.wot.DefaultWot;
import city.sane.wot.Wot;
import city.sane.wot.WotException;
import city.sane.wot.thing.ConsumedThing;
import city.sane.wot.thing.ConsumedThingException;
import city.sane.wot.thing.property.ConsumedThingProperty;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutionException;

/**
 * Fetch and consume one thing description exposes by {@link Klimabotschafter} and then observe some
 * properties.
 */
class KlimabotschafterClient {
    public static void main(String[] args) throws URISyntaxException, IOException, WotException {
        // create wot
        Wot wot = DefaultWot.clientOnly();

        URI url = new URI("coap://localhost:5683/KlimabotschafterWetterstationen:AlbrechtThaer");
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

                List<String> monitoredPropertyNames = Arrays.asList("Upload_time", "Temp_2m");
                for (String name : monitoredPropertyNames) {
                    System.out.println("Monitor changes of Property \"" + name + "\"");
                    ConsumedThingProperty<Object> property = consumedThing.getProperty(name);
                    Object value = property.read().get();
                    System.out.println("Current value of \"" + name + "\" is " + value);
                    property.subscribe(newValue -> System.out.println("Value of \"" + name + "\" has changed to " + newValue));
                }
            }
            catch (InterruptedException ex) {
                Thread.currentThread().interrupt();
            }
            catch (ExecutionException | ConsumedThingException ex) {
                throw new RuntimeException(ex);
            }
        }).join();

        System.out.println("Press ENTER to exit the client");
        System.in.read();
    }
}
