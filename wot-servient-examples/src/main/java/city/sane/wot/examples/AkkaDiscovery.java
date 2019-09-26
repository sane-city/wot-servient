package city.sane.wot.examples;

import city.sane.wot.DefaultWot;
import city.sane.wot.Wot;
import city.sane.wot.thing.ExposedThing;
import city.sane.wot.thing.Thing;

/**
 * This example exposes a Thing that can be discovered by other Actor Systems.
 */
public class AkkaDiscovery {
    public static void main(String[] args) {
        // create wot
        Wot wot = new DefaultWot();

        // create thing
        Thing thing = new Thing.Builder()
                .setId("HelloWorld")
                .setTitle("HelloWorld")
                .build();
        ExposedThing exposedThing = wot.produce(thing);

        System.out.println("Produced " + exposedThing.getTitle());

        exposedThing.expose().join();
    }
}
