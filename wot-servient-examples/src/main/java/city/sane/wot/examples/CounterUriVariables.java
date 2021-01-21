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
import city.sane.wot.thing.property.ThingProperty;
import city.sane.wot.thing.schema.ObjectSchema;

import java.util.Map;

/**
 * Produces and exposes a counter thing with variables for interaction.
 */
@SuppressWarnings({ "java:S106", "java:S1192" })
class CounterUriVariables {
    public static void main(String[] args) throws WotException {
        // create wot
        Wot wot = new DefaultWot();

        // create counter
        Thing thing = new Thing.Builder()
                .setId("counter")
                .setTitle("counter")
                .setDescription("counter example Thing")
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

        exposedThing.addAction("increment",
                new ThingAction.Builder()
                        .setDescription("Incrementing counter value with optional step value as uriVariable")
                        .setUriVariables(Map.of(
                                "step", Map.of(
                                        "type", "integer",
                                        "minimum", 1,
                                        "maximum", 250
                                )
                        ))
                        .setInput(new ObjectSchema())
                        .build(),
                (input, options) -> {
                    System.out.println("CounterUriVariables: Incrementing, input= " + input + ", options= " + options);
                    exposedThing.getProperty("count").read().thenApply(value -> {
                        int step;
                        if (input != null && ((Map) input).containsKey("step")) {
                            step = (Integer) ((Map) input).get("step");
                        }
                        else if (options.containsKey("uriVariables") && options.get("uriVariables").containsKey("step")) {
                            step = (int) options.get("uriVariables").get("step");
                        }
                        else {
                            step = 1;
                        }

                        int newValue = ((Integer) value) + step;
                        exposedThing.getProperty("count").write(newValue);
                        return newValue;
                    });
                }
        );

        exposedThing.addAction("decrement",
                new ThingAction.Builder()
                        .setDescription("Decrementing counter value with optional step value as uriVariable")
                        .setUriVariables(Map.of(
                                "step", Map.of(
                                        "type", "integer",
                                        "minimum", 1,
                                        "maximum", 250
                                )
                        ))
                        .build(),
                (input, options) -> {
                    System.out.println("CounterUriVariables: Decrementing, input= " + input + ", options= " + options);
                    exposedThing.getProperty("count").read().thenApply(value -> {
                        int step = 1;
                        if (options.get("uriVariables") != null && options.get("uriVariables").get("step") != null) {
                            step = (int) options.get("uriVariables").get("step");
                        }
                        int newValue = ((Integer) value) - step;
                        exposedThing.getProperty("count").write(newValue);
                        return newValue;
                    });
                }
        );

        exposedThing.expose();
    }
}
