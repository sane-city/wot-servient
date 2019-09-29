package city.sane.wot.examples;

import city.sane.wot.DefaultWot;
import city.sane.wot.Wot;
import city.sane.wot.thing.ConsumedThing;
import city.sane.wot.thing.Context;
import city.sane.wot.thing.ExposedThing;
import city.sane.wot.thing.Thing;
import city.sane.wot.thing.filter.SparqlThingQuery;
import city.sane.wot.thing.filter.ThingFilter;
import city.sane.wot.thing.filter.ThingQuery;
import city.sane.wot.thing.filter.ThingQueryException;
import city.sane.wot.thing.property.ThingProperty;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ExecutionException;

/**
 * This examples uses Akka's cluster functionality to discovery (remote) things exposed by {@link AkkaDiscovery}.
 */
public class AkkaDiscoveryClient {
    public static void main(String[] args) throws ExecutionException, InterruptedException, ThingQueryException {
        Wot wot = DefaultWot.clientOnly();

        // Expose a thing
        Thing cthing = new Thing.Builder()
                .setId("HelloClient")
                .setTitle("HelloClient")
                .setObjectType("Thing")
                .setObjectContext(new Context("https://www.w3.org/2019/wot/td/v1")
                        .addContext("saref", "https://w3id.org/saref#")
                )
                .build();
        ExposedThing ecthing = wot.produce(cthing);

        ecthing.addProperty("Temperature", new ThingProperty.Builder().setObjectType("saref:Temperature").build(), 15);
        ecthing.addProperty("Luftdruck", new ThingProperty.Builder().setObjectType("saref:Pressure").build(), 32);

        System.out.println(ecthing.toJson());

        ecthing.expose();

        // Search for things providing a Temperature
        ThingQuery query = new SparqlThingQuery("?x <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <https://w3id.org/saref#Temperature> .");
        Collection<Thing> things = wot.discover(new ThingFilter().setQuery(query)).get();


        System.out.println("Found " + things.size() + " thing(s)");

        if (!things.isEmpty()) {
            // print found things
            things.stream().forEach(t -> {
                System.out.println("=== TD ===");
                System.out.println(t.toJson(true));
                ConsumedThing ct = wot.consume(t);

                try {
                    Map<String, Object> properties = ct.readProperties().get();
                    properties.forEach((key, value) -> {
                        System.out.println(key + ": " + value);
                    });
                }
                catch (InterruptedException e) {
                    e.printStackTrace();
                }
                catch (ExecutionException e) {
                    e.printStackTrace();
                }

                System.out.println("==========");
            });
        }
    }
}
