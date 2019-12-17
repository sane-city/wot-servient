package city.sane.wot.binding.jadex;

import city.sane.wot.binding.ProtocolServer;
import city.sane.wot.binding.ProtocolServerException;
import city.sane.wot.thing.ExposedThing;
import com.typesafe.config.Config;
import jadex.bridge.IExternalAccess;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * Allows exposing Things via Jadex Micro Agents.<br>
 * Starts a Jadex Platform and a {@link ThingsAgent}. This Agent is responsible for exposing Things. The Jadex Platform automatically finds all other platforms
 * and thus enables interaction with their Things.
 * The Jadex Platform can be configured via the configuration parameter "wot.servient.jadex.server".
 */
public class JadexProtocolServer implements ProtocolServer {
    private static final Logger log = LoggerFactory.getLogger(JadexProtocolServer.class);

    private final Map<String, ExposedThing> things = new HashMap<>();
    private final JadexProtocolServerConfig config;
    private IExternalAccess platform;
    private ThingsService thingsService;

    public JadexProtocolServer(Config wotConfig) {
        this(new JadexProtocolServerConfig(wotConfig));
    }

    JadexProtocolServer(JadexProtocolServerConfig config) {
        this.config = config;
    }

    @Override
    public CompletableFuture<Void> start() {
        log.info("JadexServer is starting Jadex Platform");

        return config.createPlatform(things).thenAccept(result -> {
            platform = result.first();
            thingsService = result.second();
        });
    }

    @Override
    public CompletableFuture<Void> stop() {
        log.info("JadexServer is stopping Jadex Platform '{}'", platform);

        if (platform != null) {
            return FutureConverters.fromJadex(platform.killComponent()).thenApply(r -> null);
        }
        else {
            return CompletableFuture.completedFuture(null);
        }
    }

    @Override
    public CompletableFuture<Void> expose(ExposedThing thing) {
        log.info("AkkaServer exposes '{}'", thing.getTitle());
        things.put(thing.getId(), thing);

        if (platform == null) {
            return CompletableFuture.failedFuture(new ProtocolServerException("Unable to expose thing before JadexServer has been started"));
        }

        CompletableFuture<IExternalAccess> expose = FutureConverters.fromJadex(thingsService.expose(thing.getId()));

        return expose.thenApply(r -> null);
    }

    @Override
    public CompletableFuture<Void> destroy(ExposedThing thing) {
        things.remove(thing.getId());

        if (thingsService != null) {
            return FutureConverters.fromJadex(thingsService.destroy(thing.getId()));
        }
        else {
            return CompletableFuture.completedFuture(null);
        }
    }

}
