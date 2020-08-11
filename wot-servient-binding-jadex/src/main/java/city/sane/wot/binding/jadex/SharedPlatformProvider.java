package city.sane.wot.binding.jadex;

import city.sane.RefCountResource;
import com.typesafe.config.Config;
import jadex.bridge.IExternalAccess;

import java.util.HashMap;
import java.util.Map;

/**
 * This is a Singleton class, which is used by {@link JadexProtocolClient} and {@link
 * JadexProtocolServer} to share a single Jadex Platform.
 */
public class SharedPlatformProvider {
    private static final Map<Config, RefCountResource<IExternalAccess>> singletons = new HashMap<>();

    private SharedPlatformProvider() {
        // singleton class
    }

    public static synchronized RefCountResource<IExternalAccess> singleton(Config config) {
        return singletons.computeIfAbsent(
                config,
                myConfig -> new RefCountResource<>(
                        () -> {
                            JadexProtocolPlatformConfig jadexConfig = new JadexProtocolPlatformConfig(myConfig);
                            return jadexConfig.createPlatform();
                        },
                        platform -> FutureConverters.fromJadex(platform.killComponent()).join()
                )
        );
    }
}
