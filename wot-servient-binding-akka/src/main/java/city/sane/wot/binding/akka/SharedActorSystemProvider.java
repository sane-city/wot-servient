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
package city.sane.wot.binding.akka;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import city.sane.RefCountResource;
import city.sane.Triple;
import city.sane.wot.binding.akka.actor.ThingsActor;
import city.sane.wot.thing.ExposedThing;
import com.typesafe.config.Config;
import scala.compat.java8.FutureConverters;

import java.util.HashMap;
import java.util.Map;

/**
 * This is a Singleton class, which is used by {@link AkkaProtocolClient} and {@link
 * AkkaProtocolServer} to share a single ActorSystem.
 */
public class SharedActorSystemProvider {
    private static RefCountResource<Triple<ActorSystem, Map<String, ExposedThing>, ActorRef>> singleton = null;

    private SharedActorSystemProvider() {
        // singleton class
    }

    public static synchronized RefCountResource<Triple<ActorSystem, Map<String, ExposedThing>, ActorRef>> singleton(
            Config config) {
        if (singleton == null) {
            singleton = new RefCountResource<>(
                    () -> {
                        ActorSystem system = ActorSystem.create(config.getString("wot.servient.akka.system-name"), config.getConfig("wot.servient"));
                        Map<String, ExposedThing> things = new HashMap<>();
                        ActorRef thingsActor = system.actorOf(ThingsActor.props(things), "things");

                        return new Triple(system, things, thingsActor);
                    },
                    triple -> FutureConverters.toJava(triple.first().terminate()).toCompletableFuture().join()
            );
        }
        return singleton;
    }
}
