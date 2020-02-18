package city.sane.wot.binding.jadex;

import city.sane.wot.binding.ProtocolClient;
import city.sane.wot.binding.ProtocolClientFactory;
import com.typesafe.config.Config;
import jadex.bridge.IExternalAccess;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * Creates new {@link JadexProtocolClient} instances. A Jadex Platform is created for this
 * purpose.<br> The Jadex Platform can be configured via the configuration parameter
 * "wot.servient.jadex.client".
 */
public class JadexProtocolClientFactory implements ProtocolClientFactory {
    private static final Logger log = LoggerFactory.getLogger(JadexProtocolClientFactory.class);
    private final JadexProtocolClientConfig config;
    private IExternalAccess platform;

    public JadexProtocolClientFactory(Config wotConfig) {
        this(new JadexProtocolClientConfig(wotConfig));
    }

    public JadexProtocolClientFactory(JadexProtocolClientConfig config) {
        this.config = config;
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
        CompletableFuture<IExternalAccess> result = config.createPlatform();
        return result.thenApply(ia -> {
            platform = ia;
            return null;
        });
    }

    @Override
    public CompletableFuture<Void> destroy() {
        log.debug("Kill Jadex Platform");
        CompletableFuture<Map<String, Object>> result = FutureConverters.fromJadex(platform.killComponent());
        return result.thenApply(ia -> null);
    }
}
