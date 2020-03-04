package city.sane.wot.binding.jadex;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigObject;
import jadex.base.IPlatformConfiguration;
import jadex.base.PlatformConfigurationHandler;
import jadex.base.Starter;
import jadex.bridge.IExternalAccess;

public class JadexProtocolPlatformConfig {
    private final IPlatformConfiguration config;

    public JadexProtocolPlatformConfig(Config wotConfig) {
        config = PlatformConfigurationHandler.getDefault();
        if (wotConfig.hasPath("wot.servient.jadex")) {
            ConfigObject objects = wotConfig.getObject("wot.servient.jadex");
            objects.forEach((key, value) -> config.setValue(key, value.unwrapped()));
        }
    }

    public IExternalAccess createPlatform() {
        return Starter.createPlatform(config).get();
    }
}