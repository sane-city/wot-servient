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

import java.net.URISyntaxException;
import java.util.concurrent.ExecutionException;

/**
 * Fetch thing description exposes by {@link Counter} and then interact with it.
 */
@SuppressWarnings({ "java:S106", "java:S1192" })
class CounterClient {
    public static void main(String[] args) throws URISyntaxException, WotException {
        // create wot
        Wot wot = DefaultWot.clientOnly();

        wot.fetch("coap://localhost:5683/counter").whenComplete((thing, e) -> {
            try {
                if (e != null) {
                    throw new RuntimeException(e);
                }

                System.out.println("=== TD ===");
                String json = thing.toJson(true);
                System.out.println(json);
                System.out.println("==========");

                ConsumedThing consumedThing = wot.consume(thing);

                // read property #1
                Object read1 = consumedThing.getProperty("count").read().get();
                System.out.println("CounterClient: count value is " + read1);

                // increment property #1
                consumedThing.getAction("increment").invoke().get();
                Object inc1 = consumedThing.getProperty("count").read().get();
                System.out.println("CounterClient: count value after increment #1 is " + inc1);

                // increment property #2
                consumedThing.getAction("increment").invoke().get();
                Object inc2 = consumedThing.getProperty("count").read().get();
                System.out.println("CounterClient: count value after increment #2 is " + inc2);

                // decrement property
                consumedThing.getAction("decrement").invoke().get();
                Object dec1 = consumedThing.getProperty("count").read().get();
                System.out.println("CounterClient: count value after decrement is " + dec1);
            }
            catch (InterruptedException ex) {
                Thread.currentThread().interrupt();
            }
            catch (ExecutionException ex) {
                throw new RuntimeException(ex);
            }
        }).join();
    }
}
