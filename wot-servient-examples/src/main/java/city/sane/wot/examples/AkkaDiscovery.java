package city.sane.wot.examples;

import city.sane.wot.DefaultWot;
import city.sane.wot.Wot;
import city.sane.wot.WotException;
import city.sane.wot.thing.Context;
import city.sane.wot.thing.ExposedThing;
import city.sane.wot.thing.Thing;
import city.sane.wot.thing.property.ThingProperty;

/**
 * This example exposes a Thing that can be discovered by other Actor Systems.
 */
@SuppressWarnings("squid:S106")
class AkkaDiscovery {
    public static void main(String[] args) throws WotException {
        // create wot
        Wot wot = DefaultWot.serverOnly();

        // create thing
        Thing thing = new Thing.Builder()
                .setId("HelloClient")
                .setTitle("HelloClient")
                .setObjectType("Thing")
                .setObjectContext(new Context("https://www.w3.org/2019/wot/td/v1")
                        .addContext("saref", "https://w3id.org/saref#")
                )
                .build();
        ExposedThing exposedThing = wot.produce(thing);

        exposedThing.addProperty("Temperature", new ThingProperty.Builder().setObjectType("saref:Temperature").build(), 15);
        exposedThing.addProperty("Luftdruck", new ThingProperty.Builder().setObjectType("saref:Pressure").build(), 32);

        System.out.println(exposedThing.toJson(true));

        exposedThing.expose();
    }
}
