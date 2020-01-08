package city.sane.wot.examples;

import city.sane.wot.DefaultWot;
import city.sane.wot.Wot;
import city.sane.wot.WotException;
import city.sane.wot.thing.ExposedThing;
import city.sane.wot.thing.Thing;
import city.sane.wot.thing.action.ThingAction;
import city.sane.wot.thing.event.ThingEvent;
import city.sane.wot.thing.property.ThingProperty;

import java.util.Date;

/**
 * Produces and exposes a counter thing.
 */
class Counter {
    public static void main(String[] args) throws WotException {
        // create wot
        Wot wot = DefaultWot.serverOnly();

        // create counter
        Thing thing = new Thing.Builder()
                .setId("counter")
                .setTitle("counter")
                .build();
        ExposedThing exposedThing = wot.produce(thing);

        System.out.println("Produced " + exposedThing.getTitle());

        // expose counter
        exposedThing.addProperty("count",
                new ThingProperty.Builder()
                        .setType("integer")
                        .setDescription("current counter value")
                        .setObservable(true)
                        .setReadOnly(true)
                        .build(),
                42);

        exposedThing.addProperty("lastChange",
                new ThingProperty.Builder()
                        .setType("string")
                        .setDescription("last change of counter value")
                        .setObservable(true)
                        .setReadOnly(true)
                        .build(),
                new Date().toString());

        exposedThing.addAction("increment", new ThingAction(),
                () -> exposedThing.getProperty("count").read().thenApply(value -> {
                        int newValue = ((Integer) value) + 1;
                        exposedThing.getProperty("count").write(newValue);
                        exposedThing.getProperty("lastChange").write(new Date().toString());
                        exposedThing.getEvent("change").emit();
                        return newValue;
                    })
        );

        exposedThing.addAction("decrement", new ThingAction(),
                () -> exposedThing.getProperty("count").read().thenApply(value -> {
                        int newValue = ((Integer) value) - 1;
                        exposedThing.getProperty("count").write(newValue);
                        exposedThing.getProperty("lastChange").write(new Date().toString());
                        exposedThing.getEvent("change").emit();
                        return newValue;
                    })
        );

        exposedThing.addAction("reset", new ThingAction(),
                () -> exposedThing.getProperty("count").write(0).whenComplete((value, e) -> {
                        exposedThing.getProperty("lastChange").write(new Date().toString());
                        exposedThing.getEvent("change").emit();
                    })
        );

        exposedThing.addEvent("change", new ThingEvent());

        exposedThing.expose().thenRun(() -> System.out.println(exposedThing.getTitle() + " ready"));
    }
}
