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
