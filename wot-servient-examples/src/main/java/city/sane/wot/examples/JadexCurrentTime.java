package city.sane.wot.examples;

import city.sane.wot.DefaultWot;
import city.sane.wot.Wot;
import city.sane.wot.WotException;
import city.sane.wot.thing.Context;
import city.sane.wot.thing.ExposedThing;
import city.sane.wot.thing.Thing;
import city.sane.wot.thing.property.ThingProperty;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Creates and exposes a thing that returns the current time.
 */
class JadexCurrentTime {
    public static void main(String[] args) throws WotException {
        // create wot
        Wot wot = new DefaultWot();

        // create & expose thing
        Thing thing = new Thing.Builder()
                .setObjectContext(new Context("https://www.w3.org/2019/wot/td/v1"))
                .setObjectType("Thing")
                .setId("JadexCurrentTime")
                .setTitle("Aktuelle Uhrzeit")
                .build();

        ExposedThing exposedThing = wot.produce(thing);

        exposedThing.addProperty("value",
                new ThingProperty.Builder()
                        .setType("string")
                        .setReadOnly(true)
                        .setDescription("Aktuelle Uhrzeit")
                        .build()
        );

        Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                String currentTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS").format(new Date());
                exposedThing.getProperty("value").write(currentTime);
            }
        }, 0, 1000);

        exposedThing.expose().join();

        System.out.println(exposedThing.toJson(true));
    }
}
