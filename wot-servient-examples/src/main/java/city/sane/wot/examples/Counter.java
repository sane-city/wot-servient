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
import city.sane.wot.thing.property.ThingProperty;

import java.util.Date;

/**
 * Produces and exposes a counter thing.
 */
@SuppressWarnings({ "squid:S106", "java:S1192" })
class Counter {
    public static void main(String[] args) throws WotException {
        // create wot
        Wot wot = new DefaultWot();

        // create counter
        Thing thing = new Thing.Builder()
                .setId("counter")
                .setTitle("counter")
                .build();
        ExposedThing exposedThing = wot.produce(thing);

        System.out.println("Produced " + exposedThing.getId());

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

        exposedThing.addAction("increment", new ThingAction<>(),
                () -> exposedThing.getProperty("count").read().thenApply(value -> {
                    int newValue = ((Integer) value) + 1;
                    exposedThing.getProperty("count").write(newValue);
                    exposedThing.getProperty("lastChange").write(new Date().toString());
                    exposedThing.getEvent("change").emit();
                    return newValue;
                })
        );

        exposedThing.addAction("decrement", new ThingAction<>(),
                () -> exposedThing.getProperty("count").read().thenApply(value -> {
                    int newValue = ((Integer) value) - 1;
                    exposedThing.getProperty("count").write(newValue);
                    exposedThing.getProperty("lastChange").write(new Date().toString());
                    exposedThing.getEvent("change").emit();
                    return newValue;
                })
        );

        exposedThing.addAction("reset", new ThingAction<>(),
                () -> exposedThing.getProperty("count").write(0).whenComplete((value, e) -> {
                    exposedThing.getProperty("lastChange").write(new Date().toString());
                    exposedThing.getEvent("change").emit();
                })
        );

        exposedThing.addEvent("change", new ThingEvent<>());

        exposedThing.expose().whenComplete((r, e) -> {
            if (e == null) {
                System.out.println(exposedThing.getId() + " ready");
            }
            else {
                System.err.println(e.getMessage());
            }
        });
    }
}
