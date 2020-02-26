package city.sane.wot.binding.akka;

import akka.actor.ActorSystem;
import city.sane.RefCountResource;
import com.typesafe.config.Config;
import scala.compat.java8.FutureConverters;

/**
 * This is a Singleton class, which is used by {@link AkkaProtocolClient} and {@link
 * AkkaProtocolServer} to share a single ActorSystem.
 */
public class SharedActorSystemProvider {
    private static RefCountResource<ActorSystem> singleton = null;

    private SharedActorSystemProvider() {
        // singleton class
    }

    public static synchronized RefCountResource<ActorSystem> singleton(Config config) {
        if (singleton == null) {
            singleton = new RefCountResource<>(
                    () -> ActorSystem.create(config.getString("wot.servient.akka.system-name"), config.getConfig("wot.servient")),
                    system -> FutureConverters.toJava(system.terminate()).toCompletableFuture().join()
            );
        }
        return singleton;
    }
}
