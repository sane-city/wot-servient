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
import city.sane.wot.thing.Context;
import city.sane.wot.thing.ExposedThing;
import city.sane.wot.thing.Thing;
import city.sane.wot.thing.property.ThingProperty;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;

/**
 * This example exposes a Thing that can be discovered by other Actor Systems.
 */
@SuppressWarnings("squid:S106")
class AkkaDiscovery {
    public static void main(String[] args) throws WotException {
        // create wot
        Config config = ConfigFactory
                .parseString("wot.servient.servers = [\"city.sane.wot.binding.akka.AkkaProtocolServer\"]")
                .withFallback(ConfigFactory.load());
        Wot wot = DefaultWot.serverOnly(config);

        // create thing
        Thing thing = new Thing.Builder()
                .setId("HelloClient")
                .setTitle("HelloClient")
                .setObjectType("Thing")
                .setObjectContext(new Context("https://www.w3.org/2019/wot/td/v1")
                        .addContext("saref", "https://w3id.org/saref#")
                )
                .build();
        ExposedThing exposedThing = wot.produce(thing);

        exposedThing.addProperty("Temperature", new ThingProperty.Builder().setObjectType("saref:Temperature").build(), 15);
        exposedThing.addProperty("Luftdruck", new ThingProperty.Builder().setObjectType("saref:Pressure").build(), 32);

        System.out.println(exposedThing.toJson(true));

        exposedThing.expose();
    }
}
