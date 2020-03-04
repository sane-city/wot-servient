package city.sane.wot.binding.jadex;

import city.sane.RefCountResource;
import com.typesafe.config.Config;
import jadex.bridge.IExternalAccess;

/**
 * This is a Singleton class, which is used by {@link JadexProtocolClient} and {@link
 * JadexProtocolServer} to share a single Jadex Platform.
 */
public class SharedPlatformProvider {
    private static RefCountResource<IExternalAccess> singleton = null;

    private SharedPlatformProvider() {
        // singleton class
    }

    public static synchronized RefCountResource<IExternalAccess> singleton(Config config) {
        if (singleton == null) {
            JadexProtocolPlatformConfig jadexConfig = new JadexProtocolPlatformConfig(config);
            singleton = new RefCountResource<>(
                    () -> jadexConfig.createPlatform(),
                    platform -> FutureConverters.fromJadex(platform.killComponent()).join()
            );
        }
        return singleton;
    }
}
