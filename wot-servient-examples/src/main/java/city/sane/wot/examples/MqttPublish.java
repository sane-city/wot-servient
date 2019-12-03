package city.sane.wot.examples;

import city.sane.wot.DefaultWot;
import city.sane.wot.Wot;
import city.sane.wot.WotException;
import city.sane.wot.thing.ExposedThing;
import city.sane.wot.thing.Thing;
import city.sane.wot.thing.event.ThingEvent;

import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Produces a thing that sends a ascending counter to an MQTT topic.
 */
public class MqttPublish {
    public static void main(String[] args) throws WotException {
        System.out.println("Setup MQTT broker address/port details in application.json!");

        // create wot
        Wot wot = new DefaultWot();

        Thing thing = new Thing.Builder()
                .setId("MQTT-Test")
                .setTitle("MQTT-Test")
                .setDescription("Tests a MQTT client that published counter values as an WoT event and subscribes the " +
                        "resetCounter topic as WoT action to reset the own counter.")
                .build();

        ExposedThing exposedThing = wot.produce(thing);

        AtomicInteger counter = new AtomicInteger();
        exposedThing
                .addAction("resetCounter", (input, options) -> {
                    System.out.println("Resetting counter");
                    counter.set(0);
                    return null;
                })
                .addEvent("counterEvent", new ThingEvent.Builder()
                        .setType("integer")
                        .build()
                );

        exposedThing.expose().whenComplete((result, ex) -> {
            System.out.println(exposedThing.getTitle() + " ready");

            System.out.println("=== TD ===");
            String json = exposedThing.toJson(true);
            System.out.println(json);
            System.out.println("==========");

            Timer timer = new Timer();
            timer.schedule(new TimerTask() {
                @Override
                public void run() {
                    int newCount = counter.incrementAndGet();
                    exposedThing.getEvent("counterEvent").emit(newCount);
                    System.out.println("New count " + newCount);
                }
            }, 0, 1000);
        });
    }
}
