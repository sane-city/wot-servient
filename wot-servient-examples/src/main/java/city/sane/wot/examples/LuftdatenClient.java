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
import city.sane.wot.thing.Thing;
import city.sane.wot.thing.filter.DiscoveryMethod;
import city.sane.wot.thing.filter.ThingFilter;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

/**
 * Fetch and consume first thing description exposes by {@link Luftdaten} and then read some
 * properties.
 */
@SuppressWarnings({ "java:S106" })
class LuftdatenClient {
    public static void main(String[] args) throws URISyntaxException, ExecutionException, InterruptedException, WotException {
        // create wot
        Wot wot = DefaultWot.clientOnly();

        ThingFilter filter = new ThingFilter()
                .setMethod(DiscoveryMethod.DIRECTORY)
                .setUrl(new URI("coap://localhost"));
        Thing thing = wot.discover(filter).firstElement().blockingGet();

        System.out.println("=== TD ===");
        String json = thing.toJson(true);
        System.out.println(json);
        System.out.println("==========");

        ConsumedThing consumedThing = wot.consume(thing);

        CompletableFuture<Object> future1 = consumedThing.getProperty("latitude").read();
        Object latitude = future1.get();
        System.out.println("latitude = " + latitude);

        CompletableFuture<Object> future2 = consumedThing.getProperty("P1").read();
        Object p1 = future2.get();
        System.out.println("P1 = " + p1);
    }
}
