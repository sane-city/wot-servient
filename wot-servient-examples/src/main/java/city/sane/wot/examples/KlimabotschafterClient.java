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
import city.sane.wot.thing.property.ConsumedThingProperty;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutionException;

/**
 * Fetch and consume one thing description exposes by {@link Klimabotschafter} and then observe some
 * properties.
 */
@SuppressWarnings({ "java:S106" })
class KlimabotschafterClient {
    public static void main(String[] args) throws URISyntaxException, IOException, WotException {
        // create wot
        Wot wot = DefaultWot.clientOnly();

        URI url = new URI("coap://localhost:5683/KlimabotschafterWetterstationen:AlbrechtThaer");
        wot.fetch(url).whenComplete((thing, e) -> {
            try {
                if (e != null) {
                    throw new RuntimeException(e);
                }

                System.out.println("=== TD ===");
                String json = thing.toJson(true);
                System.out.println(json);
                System.out.println("==========");

                ConsumedThing consumedThing = wot.consume(thing);

                List<String> monitoredPropertyNames = Arrays.asList("Upload_time", "Temp_2m");
                for (String name : monitoredPropertyNames) {
                    System.out.println("Monitor changes of Property \"" + name + "\"");
                    ConsumedThingProperty<Object> property = consumedThing.getProperty(name);
                    Object value = property.read().get();
                    System.out.println("Current value of \"" + name + "\" is " + value);
                    property.observer().subscribe(newValue -> System.out.println("Value of \"" + name + "\" has changed to " + newValue));
                }
            }
            catch (InterruptedException ex) {
                Thread.currentThread().interrupt();
            }
            catch (ExecutionException | ConsumedThingException ex) {
                throw new RuntimeException(ex);
            }
        }).join();

        System.out.println("Press ENTER to exit the client");
        System.in.read();
    }
}
