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
import city.sane.wot.thing.action.ThingAction;
import city.sane.wot.thing.event.ThingEvent;
import city.sane.wot.thing.schema.VariableDataSchema;

import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Produces and exposes a thing that will fire an event every few seconds.
 */
@SuppressWarnings({ "java:S106", "java:S112" })
class ExampleEvent {
    public static void main(String[] args) throws WotException {
        // create wot
        Wot wot = new DefaultWot();

        // create counter
        Thing thing = new Thing.Builder()
                .setId("EventSource")
                .setTitle("EventSource")
                .build();
        ExposedThing exposedThing = wot.produce(thing);

        System.out.println("Produced " + exposedThing.getId());

        AtomicInteger counter = new AtomicInteger();
        exposedThing.addAction("reset", new ThingAction<>(),
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
