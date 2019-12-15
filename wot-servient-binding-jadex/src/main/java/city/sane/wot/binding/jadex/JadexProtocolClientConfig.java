package city.sane.wot.binding.jadex;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigObject;
import jadex.base.IPlatformConfiguration;
import jadex.base.PlatformConfigurationHandler;
import jadex.base.Starter;
import jadex.bridge.IExternalAccess;

import java.util.concurrent.CompletableFuture;

public class JadexProtocolClientConfig {
    private final IPlatformConfiguration config;

    public JadexProtocolClientConfig(Config wotConfig) {
        config = PlatformConfigurationHandler.getDefault();
        if (wotConfig.hasPath("wot.servient.jadex.client")) {
            ConfigObject objects = wotConfig.getObject("wot.servient.jadex.client");
            objects.forEach((key, value) -> config.setValue(key, value.unwrapped()));
        }
    }

    public CompletableFuture<IExternalAccess> createPlatform() {
        return FutureConverters.fromJadex(Starter.createPlatform(config));
    }
}