package city.sane.wot.binding.jadex;

import city.sane.Pair;
import city.sane.wot.thing.ExposedThing;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigObject;
import jadex.base.IPlatformConfiguration;
import jadex.base.PlatformConfigurationHandler;
import jadex.base.Starter;
import jadex.bridge.IExternalAccess;
import jadex.bridge.service.search.ServiceQuery;
import jadex.bridge.service.types.cms.CreationInfo;

import java.util.Map;
import java.util.concurrent.CompletableFuture;

public class JadexProtocolServerConfig {
    private final IPlatformConfiguration config;

    public JadexProtocolServerConfig(Config wotConfig) {
        config = PlatformConfigurationHandler.getDefault();
        ConfigObject objects = wotConfig.getObject("wot.servient.jadex.server");
        objects.forEach((key, value) -> config.setValue(key, value.unwrapped()));
    }

    public CompletableFuture<Pair<IExternalAccess, ThingsService>> createPlatform(Map<String, ExposedThing> things) {
        CompletableFuture<IExternalAccess> createPlatform = FutureConverters.fromJadex(Starter.createPlatform(config));

        CompletableFuture<IExternalAccess> createThingsAgent = createPlatform.thenCompose(agent -> {
            CreationInfo info = new CreationInfo()
                    .setFilenameClass(ThingsAgent.class)
                    .addArgument("things", things);
            return FutureConverters.fromJadex(agent.createComponent(info));
        });

        CompletableFuture<ThingsService> searchThingsService = createThingsAgent.thenCompose(agent -> FutureConverters
                .fromJadex(agent.searchService(new ServiceQuery<>(ThingsService.class))));

        return createPlatform
                .thenCombine(searchThingsService, (ia, service) -> new Pair(ia, service));
    }
}
