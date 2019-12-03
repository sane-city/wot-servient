package city.sane.wot.examples;

import city.sane.wot.DefaultWot;
import city.sane.wot.Wot;
import city.sane.wot.WotException;
import city.sane.wot.thing.ExposedThing;
import city.sane.wot.thing.Thing;
import city.sane.wot.thing.action.ThingAction;
import city.sane.wot.thing.event.ThingEvent;
import city.sane.wot.thing.schema.VariableDataSchema;

import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Produces and exposes a thing that will fire an event every few seconds.
 */
public class ExampleEvent {
    public static void main(String[] args) throws WotException {
        // create wot
        Wot wot = new DefaultWot();

        // create counter
        Thing thing = new Thing.Builder()
                .setId("EventSource")
                .setTitle("EventSource")
                .build();
        ExposedThing exposedThing = wot.produce(thing);

        System.out.println("Produced " + exposedThing.getTitle());

        AtomicInteger counter = new AtomicInteger();
        exposedThing.addAction("reset", new ThingAction(),
                () -> {
                    System.out.println("Resetting");
                    counter.set(0);
                }
        );
        exposedThing.addEvent("onchange",
                new ThingEvent.Builder()
                        .setData(new VariableDataSchema.Builder()
                                .setType("integer")
                                .build()
                        ).build()
        );

        exposedThing.expose().whenComplete((result, e) -> {
            if (result != null) {
                System.out.println(exposedThing + " ready");
                Timer timer = new Timer();
                timer.schedule(new TimerTask() {
                    @Override
                    public void run() {
                        int newCount = counter.incrementAndGet();
                        exposedThing.getEvent("onchange").emit(newCount);
                        System.out.println("Emitted change " + newCount);
                    }
                }, 0, 5000);
            }
            else {
                throw new RuntimeException("Expose error: " + e.toString());
            }
        });
    }
}
