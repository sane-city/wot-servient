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
import city.sane.wot.thing.ConsumedThing;
import city.sane.wot.thing.ConsumedThingException;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

/**
 * Fetch thing description exposes by {@link ExampleEvent} and then subscribe to the event.
 */
@SuppressWarnings({ "java:S106", "java:S112" })
class ExampleEventClient {
    public static void main(String[] args) throws URISyntaxException, IOException, WotException {
        // create wot
        Wot wot = DefaultWot.clientOnly();

        URI url = new URI("coap://localhost:5683/EventSource");
        wot.fetch(url).whenComplete((thing, e) -> {
            if (e != null) {
                throw new RuntimeException(e);
            }

            System.out.println("=== TD ===");
            String json = thing.toJson(true);
            System.out.println(json);
            System.out.println("==========");

            ConsumedThing consumedThing = wot.consume(thing);

            try {
                consumedThing.getEvent("onchange").observer().subscribe(
                        next -> System.out.println("ExampleDynamicClient: next = " + next),
                        ex -> System.out.println("ExampleDynamicClient: error = " + ex.toString()),
                        () -> System.out.println("ExampleDynamicClient: completed!")
                );
                System.out.println("ExampleDynamicClient: Subscribed");
            }
            catch (ConsumedThingException ex) {
                throw new RuntimeException(e);
            }
        }).join();

        System.out.println("Press ENTER to exit the client");
        System.in.read();
    }
}
