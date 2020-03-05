package city.sane.wot.examples;

import city.sane.wot.DefaultWot;
import city.sane.wot.Wot;
import city.sane.wot.WotException;
import city.sane.wot.thing.ConsumedThing;
import city.sane.wot.thing.Thing;
import city.sane.wot.thing.filter.SparqlThingQuery;
import city.sane.wot.thing.filter.ThingFilter;
import city.sane.wot.thing.filter.ThingQuery;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ExecutionException;

/**
 * This examples uses Akka's cluster functionality to discovery (remote) things exposed by {@link
 * AkkaDiscovery}.
 */
class AkkaDiscoveryClient {
    public static void main(String[] args) throws WotException {
        // create wot
        Config config = ConfigFactory
                .parseString("wot.servient.client-factories = [\"city.sane.wot.binding.akka.AkkaProtocolClientFactory\"]\nwot.servient.akka.remote.artery.canonical.port = 0")
                .withFallback(ConfigFactory.load());
        Wot wot = DefaultWot.clientOnly(config);

        // Search for things providing a Temperature
        ThingQuery query = new SparqlThingQuery("?x <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <https://w3id.org/saref#Temperature> .");
        Collection<Thing> things = wot.discover(new ThingFilter().setQuery(query)).toList().blockingGet();

        System.out.println("Found " + things.size() + " thing(s)");

        if (!things.isEmpty()) {
            // print found things
            things.forEach(t -> {
                System.out.println("=== TD ===");
                System.out.println(t.toJson(true));
                ConsumedThing ct = wot.consume(t);

                try {
                    Map<String, Object> properties = ct.readProperties().get();
                    properties.forEach((key, value) -> System.out.println(key + ": " + value));
                }
                catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
                catch (ExecutionException e) {
                    throw new RuntimeException(e);
                }

                System.out.println("==========");
            });
        }
    }
}
