package city.sane.wot.binding.jadex;

import city.sane.wot.binding.ProtocolServer;
import city.sane.wot.thing.ExposedThing;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigObject;
import jadex.base.IPlatformConfiguration;
import jadex.base.PlatformConfigurationHandler;
import jadex.base.Starter;
import jadex.bridge.IExternalAccess;
import jadex.bridge.service.search.ServiceQuery;
import jadex.bridge.service.types.cms.CreationInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
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
    static final Logger log = LoggerFactory.getLogger(JadexProtocolServer.class);

    private final Map<String, ExposedThing> things = new HashMap<>();
    private final IPlatformConfiguration platformConfig;
    private String serviceInteractionId;
    private IExternalAccess platform;
    private ThingsService thingsService;

    public JadexProtocolServer(Config config) {
        platformConfig = PlatformConfigurationHandler.getDefault();
        ConfigObject objects = config.getObject("wot.servient.jadex.server");
        objects.forEach((key, value) -> platformConfig.setValue(key, value.unwrapped()));
    }

    @Override
    public CompletableFuture<Void> start() {
        log.info("JadexServer is starting Jadex Platform");

        CompletableFuture<IExternalAccess> createPlatform = FutureConverters.fromJadex(Starter.createPlatform(platformConfig));
        CompletableFuture<IExternalAccess> createThingsAgent = createPlatform.thenCompose(agent -> {
            CreationInfo info = new CreationInfo()
                    .setFilenameClass(ThingsAgent.class)
                    .addArgument("things", things);
            return FutureConverters.fromJadex(agent.createComponent(info));
        });

        CompletableFuture<ThingsService> searchThingsService = createThingsAgent.thenCompose(agent -> FutureConverters
                .fromJadex(agent.searchService(new ServiceQuery<>(ThingsService.class))));

        CompletableFuture<ThingsService> start = createPlatform
                .thenCombine(searchThingsService, (ia, service) -> {
                    this.platform = ia;
                    this.thingsService = service;
                    return thingsService;
                });

        return start.thenApply(r -> null);
    }

    @Override
    public CompletableFuture<Void> stop() {
        log.info("JadexServer is stopping Jadex Platform '{}'", platform);

        return FutureConverters.fromJadex(platform.killComponent()).thenApply(r -> null);
    }

    @Override
    public CompletableFuture<Void> expose(ExposedThing thing) {
        log.info("AkkaServer exposes '{}'", thing.getTitle());
        things.put(thing.getId(), thing);

        if (platform == null) {
            return CompletableFuture.failedFuture(new Exception("Unable to expose thing before JadexServer has been started"));
        }

        CompletableFuture<IExternalAccess> expose = FutureConverters.fromJadex(thingsService.expose(thing.getId()));

        return expose.thenApply(r -> null);
    }

    @Override
    public CompletableFuture<Void> destroy(ExposedThing thing) {
        log.info("JadexServer stop exposing '{}' at {}", thing.getId(), buildJadexURI(serviceInteractionId, thing.getId()));
        things.remove(thing.getId());

        return FutureConverters.fromJadex(thingsService.destroy(thing.getId()));
    }

    public Map<String, ExposedThing> getThings() {
        return things;
    }

    public void setServiceInteractionId(String serviceInteractionId) {
        this.serviceInteractionId = serviceInteractionId;
    }

    public static URI buildJadexURI(String serviceId, String thingId) {
        return UriComponentsBuilder.newInstance().scheme("jadex").pathSegment(serviceId, thingId).build().encode().toUri();
    }
}
