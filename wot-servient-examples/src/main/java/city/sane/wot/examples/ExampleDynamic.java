package city.sane.wot.examples;

import city.sane.wot.DefaultWot;
import city.sane.wot.Wot;
import city.sane.wot.WotException;
import city.sane.wot.thing.ExposedThing;
import city.sane.wot.thing.Thing;
import city.sane.wot.thing.action.ThingAction;
import city.sane.wot.thing.property.ThingProperty;

/**
 * Produces and exposes a thing with change its description on interaction.
 */
public class ExampleDynamic {
    public static void main(String[] args) throws WotException {
        // create wot
        Wot wot = new DefaultWot();

        // create counter
        Thing thing = new Thing.Builder()
                .setTitle("DynamicThing")
                .build();
        ExposedThing exposedThing = wot.produce(thing);

        System.out.println("Produced " + exposedThing.getTitle());

        exposedThing.addAction("addProperty", new ThingAction(),
                () -> {
                    System.out.println("Adding Property");
                    exposedThing.addProperty("dynProperty",
                            new ThingProperty.Builder()
                                    .setType("string")
                                    .build(),
                            "available");
                }
        );

        exposedThing.addAction("remProperty", new ThingAction(),
                () -> {
                    System.out.println("Removing Property");
                    exposedThing.removeProperty("dynProperty");
                }
        );

        exposedThing.expose();
    }
}
