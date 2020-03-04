package city.sane.wot.examples;

import city.sane.wot.DefaultWot;
import city.sane.wot.Wot;
import city.sane.wot.WotException;
import city.sane.wot.thing.ConsumedThing;
import city.sane.wot.thing.Thing;
import city.sane.wot.thing.filter.JsonThingQuery;
import city.sane.wot.thing.filter.ThingFilter;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;

import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ExecutionException;

/**
 * Discover thing description exposes by {@link JadexCurrentTime} and then interact with it.
 */
class JadexCurrentTimeClient {
    public static void main(String[] args) throws WotException {
        // create wot
        Config config = ConfigFactory
                .parseString("wot.servient.client-factories = [\"city.sane.wot.binding.jadex.JadexProtocolClientFactory\"]")
                .withFallback(ConfigFactory.load());
        Wot wot = DefaultWot.clientOnly(config);

        ThingFilter filter = new ThingFilter()
                .setQuery(new JsonThingQuery("{\"name\":\"JadexCurrentTime\"}"));
        List<Thing> things = wot.discover(filter).toList().blockingGet();

        // get first
        Thing thing = things.stream().findFirst().get();

        System.out.println("=== TD ===");
        String json = thing.toJson(true);
        System.out.println(json);
        System.out.println("==========");

        ConsumedThing consumedThing = wot.consume(thing);

        Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                try {
                    Object currentTime = consumedThing.getProperty("value").read().get();
                    System.out.println("Current time is: " + currentTime);
                }
                catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
                catch (ExecutionException e) {
                    throw new RuntimeException(e);
                }
            }
        }, 0, 1000);
    }
}
