package city.sane.wot.binding.jadex;

import city.sane.RefCountResource;
import city.sane.RefCountResourceException;
import city.sane.wot.binding.ProtocolClient;
import city.sane.wot.binding.ProtocolClientFactory;
import com.typesafe.config.Config;
import jadex.bridge.IExternalAccess;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;

import static java.util.concurrent.CompletableFuture.completedFuture;
import static java.util.concurrent.CompletableFuture.runAsync;

/**
 * Creates new {@link JadexProtocolClient} instances. A Jadex Platform is created for this
 * purpose.<br> The Jadex Platform can be configured via the configuration parameter
 * "wot.servient.jadex.client".
 */
public class JadexProtocolClientFactory implements ProtocolClientFactory {
    private static final Logger log = LoggerFactory.getLogger(JadexProtocolClientFactory.class);
    private final RefCountResource<IExternalAccess> platformProvider;
    private IExternalAccess platform = null;

    public JadexProtocolClientFactory(Config wotConfig) {
        this(SharedPlatformProvider.singleton(wotConfig));
    }

    public JadexProtocolClientFactory(RefCountResource<IExternalAccess> platformProvider) {
        this.platformProvider = platformProvider;
    }

    @Override
    public String toString() {
        return "JadexClient";
    }

    @Override
    public String getScheme() {
        return "jadex";
    }

    @Override
    public ProtocolClient getClient() {
        return new JadexProtocolClient(platform);
    }

    @Override
    public CompletableFuture<Void> init() {
        log.debug("Create Jadex Platform");
        return runAsync(() -> {
            try {
                platform = platformProvider.retain();
            }
            catch (RefCountResourceException e) {
                throw new CompletionException(e);
            }
        });
    }

    @Override
    public CompletableFuture<Void> destroy() {
        log.debug("Kill Jadex Platform");

        if (platform != null) {
            return runAsync(() -> {
                try {
                    platformProvider.release();
                }
                catch (RefCountResourceException e) {
                    throw new CompletionException(e);
                }
            });
        }
        else {
            return completedFuture(null);
        }
    }

    public IExternalAccess getJadexPlatform() {
        return platform;
    }
}
