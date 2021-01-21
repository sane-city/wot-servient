/*
 * Copyright (c) 2021.
 *
 * This file is part of SANE Web of Things Servient.
 *
 * SANE Web of Things Servient is free software: you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * SANE Web of Things Servient is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with SANE Web of Things Servient.  If not, see
 * <http://www.gnu.org/licenses/>.
 */
package city.sane.wot.examples;

import city.sane.wot.DefaultWot;
import city.sane.wot.Wot;
import city.sane.wot.WotException;
import city.sane.wot.thing.ExposedThing;
import city.sane.wot.thing.Thing;
import city.sane.wot.thing.event.ThingEvent;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;

import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Produces a thing that sends a ascending counter to an MQTT topic.
 */
@SuppressWarnings({ "java:S106" })
class MqttPublish {
    public static void main(String[] args) throws WotException {
        // Setup MQTT broker address/port details in application.json!

        // create wot
        Config config = ConfigFactory
                .parseString("wot.servient.servers = [\"city.sane.wot.binding.mqtt.MqttProtocolServer\"]")
                .withFallback(ConfigFactory.load());
        Wot wot = DefaultWot.serverOnly(config);

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
            System.out.println(exposedThing.getId() + " ready");

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
