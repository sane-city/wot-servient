package city.sane.wot.examples;

import city.sane.wot.DefaultWot;
import city.sane.wot.Wot;
import city.sane.wot.WotException;
import city.sane.wot.content.Content;
import city.sane.wot.content.ContentCodecException;
import city.sane.wot.content.ContentManager;
import city.sane.wot.thing.ConsumedThing;
import city.sane.wot.thing.ConsumedThingException;
import city.sane.wot.thing.Thing;
import city.sane.wot.thing.event.ThingEvent;
import city.sane.wot.thing.form.Form;
import city.sane.wot.thing.schema.ArraySchema;
import city.sane.wot.thing.schema.ObjectSchema;

import java.util.Base64;
import java.util.List;
import java.util.Map;

/**
 * Receives data from a The Things Network-connected LoRa Sensor deployed on a HOCHBAHN bus.
 */
class BusSensor {
    public static void main(String[] args) throws ConsumedThingException, WotException {
        // create wot
        Wot wot = new DefaultWot();

        Thing thing = new Thing.Builder()
                .setId("SANE:TTN:00ac7f079c53c58b")
                .setTitle("Bus-Sensor")
                .addEvent("up",
                        new ThingEvent.Builder()
                                .setDescription("Uplink Messages")
                                .setData(new ObjectSchema())
                                .addForm(
                                        new Form.Builder()
                                                .setHref("mqtt://eu.thethings.network/sane-lora/devices/00ac7f079c53c58b/up")
                                                .build()
                                )
                                .build()
                )
                .build();

        System.out.println("=== TD ===");
        String json = thing.toJson(true);
        System.out.println(json);
        System.out.println("==========");

        ConsumedThing consumedThing = wot.consume(thing);

        consumedThing.getEvent("up").observer().subscribe(BusSensor::nextMessage);
    }

    private static void nextMessage(Object next) {
        System.out.println("BusSensor: New message received: " + next);

        // extract payload
        String payload = (String) ((Map<String, Object>) next).get("payload_raw");
        byte[] decodedPayload = Base64.getDecoder().decode(payload);
        Content content = new Content("application/cbor", decodedPayload);
        try {
            List<Number> values = ContentManager.contentToValue(content, new ArraySchema());
            System.out.println("BusSensor: values = " + values);
            // Array format
            // 0  message_type [1]
            // 1  Cycle count  [unsigned count]
            // 2  PM2.5        [1/10 µg/m^3]
            // 3  PM10         [1/10 µg/m^3]
            // 4  Temperature  [1/100 °C]
            // 5  Pressure     [Pascal]
            // 6  Humidity     [1/100 %relative humidity]
            // 7  Latitude     [float °]
            // 8  Longitude    [float °]
            // 9  TrackDegree  [float °]
            // 10 Velocity     [float km/h]
            // 11 TimeToFix    [1/10 s]
            // 12 VisibleSats  [unsigned count]
            // 13 TrackedSats  [unsigned count]

            int type = (int) values.get(0);

            if (type == 1) {
                Number cycleCount = values.get(1);
                System.out.println("Cycle Count = " + cycleCount);

                Number pm25 = values.get(2);
                System.out.println("PM2.5 (1/10 µg/m^3) = " + pm25);

                Number pm10 = values.get(3);
                System.out.println("PM10 (1/10 µg/m^3) = " + pm10);

                Number temperature = values.get(4);
                System.out.println("Temperature (1/100 °C) = " + temperature);

                Number pressure = values.get(5);
                System.out.println("Pressure (Pascal) = " + pressure);

                Number humidity = values.get(6);
                System.out.println("Humidity (1/100 %relative humidity) = " + humidity);

                Number latitude = values.get(7);
                System.out.println("Latitude (float °) = " + latitude);

                Number longitude = values.get(8);
                System.out.println("Longitude (float °) = " + longitude);

                Number trackDegre = values.get(9);
                System.out.println("Track Degree (float °) = " + trackDegre);

                Number velocity = values.get(10);
                System.out.println("Velocity (float km/h) = " + velocity);

                Number timeToFix = values.get(11);
                System.out.println("TimeToFix (1/10 s) = " + timeToFix);

                Number visibleSats = values.get(12);
                System.out.println("Visible Sats = " + visibleSats);

                Number trackedSats = values.get(13);
                System.out.println("Tracked Sats = " + trackedSats);
            }
        }
        catch (ContentCodecException e) {
            System.err.println("Unable to extract received content: " + e.getMessage());
        }
    }
}
