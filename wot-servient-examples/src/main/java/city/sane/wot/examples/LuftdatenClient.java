package city.sane.wot.examples;

import city.sane.wot.DefaultWot;
import city.sane.wot.Wot;
import city.sane.wot.WotException;
import city.sane.wot.thing.ConsumedThing;
import city.sane.wot.thing.Thing;
import city.sane.wot.thing.filter.DiscoveryMethod;
import city.sane.wot.thing.filter.ThingFilter;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collection;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

/**
 * Fetch and consume first thing description exposes by {@link Luftdaten} and then read some properties.
 */
class LuftdatenClient {
    public static void main(String[] args) throws URISyntaxException, ExecutionException, InterruptedException, WotException {
        // create wot
        Wot wot = DefaultWot.clientOnly();

        ThingFilter filter = new ThingFilter()
                .setMethod(DiscoveryMethod.DIRECTORY)
                .setUrl(new URI("coap://localhost"));
        Collection<Thing> things = wot.discover(filter).get();

        // get first
        Thing thing = things.stream().findFirst().get();

        System.out.println("=== TD ===");
        String json = thing.toJson(true);
        System.out.println(json);
        System.out.println("==========");

        ConsumedThing consumedThing = wot.consume(thing);

        CompletableFuture<Object> future1 = consumedThing.getProperty("latitude").read();
        Object latitude = future1.get();
        System.out.println("latitude = " + latitude);

        CompletableFuture<Object> future2 = consumedThing.getProperty("P1").read();
        Object p1 = future2.get();
        System.out.println("P1 = " + p1);
    }
}
