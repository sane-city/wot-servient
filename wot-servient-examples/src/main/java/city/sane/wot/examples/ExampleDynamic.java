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

/**
 * Produces and exposes a thing with change its description on interaction.
 */
@SuppressWarnings({ "java:S106" })
class ExampleDynamic {
    public static void main(String[] args) throws WotException {
        // create wot
        Wot wot = new DefaultWot();

        // create counter
        Thing thing = new Thing.Builder()
                .setTitle("DynamicThing")
                .build();
        ExposedThing exposedThing = wot.produce(thing);

        System.out.println("Produced " + exposedThing.getId());

        exposedThing.addAction("addProperty", new ThingAction<>(),
                () -> {
                    System.out.println("Adding Property");
                    exposedThing.addProperty("dynProperty",
                            new ThingProperty.Builder()
                                    .setType("string")
                                    .build(),
                            "available");
                }
        );

        exposedThing.addAction("remProperty", new ThingAction<>(),
                () -> {
                    System.out.println("Removing Property");
                    exposedThing.removeProperty("dynProperty");
                }
        );

        exposedThing.expose();
    }
}
