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
import city.sane.wot.thing.filter.JsonThingQuery;
import city.sane.wot.thing.filter.ThingFilter;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;

import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ExecutionException;

/**
 * Discover thing description exposes by {@link JadexCurrentTime} and then interact with it.
 */
@SuppressWarnings({ "java:S106", "java:S112" })
class JadexCurrentTimeClient {
    public static void main(String[] args) throws WotException {
        // create wot
        Config config = ConfigFactory
                .parseString("wot.servient.client-factories = [\"city.sane.wot.binding.jadex.JadexProtocolClientFactory\"]")
                .withFallback(ConfigFactory.load());
        Wot wot = DefaultWot.clientOnly(config);

        ThingFilter filter = new ThingFilter()
                .setQuery(new JsonThingQuery("{\"@type\":\"https://www.w3.org/2019/wot/td#Thing\"}"));

        // get first
        Thing thing = wot.discover(filter).firstElement().blockingGet();

        System.out.println("=== TD ===");
        String json = thing.toJson(true);
        System.out.println(json);
        System.out.println("==========");

        ConsumedThing consumedThing = wot.consume(thing);

        Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                try {
                    Object currentTime = consumedThing.getProperty("value").read().get();
                    System.out.println("Current time is: " + currentTime);
                }
                catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
                catch (ExecutionException e) {
                    throw new RuntimeException(e);
                }
            }
        }, 0, 1000);
    }
}
