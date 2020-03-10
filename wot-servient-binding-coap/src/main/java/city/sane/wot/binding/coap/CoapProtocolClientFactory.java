package city.sane.wot.binding.coap;

import city.sane.wot.binding.ProtocolClientFactory;
import com.typesafe.config.Config;
import org.eclipse.californium.core.CoapClient;
import org.slf4j.bridge.SLF4JBridgeHandler;

import java.time.Duration;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Creates new {@link CoapProtocolClient} instances.
 */
public class CoapProtocolClientFactory implements ProtocolClientFactory {
    static {
        // Californium uses java.util.logging. We need to redirect all log messages to logback
        SLF4JBridgeHandler.removeHandlersForRootLogger();
        SLF4JBridgeHandler.install();
    }

    private final ExecutorService executor;
    private final Duration timeout;

    public CoapProtocolClientFactory(Config config) {
        this(
                Executors.newFixedThreadPool(10),
                config.getDuration("wot.servient.coap.timeout")
        );
    }

    CoapProtocolClientFactory(ExecutorService executor, Duration timeout) {
        this.executor = executor;
        this.timeout = timeout;
    }

    @Override
    public String toString() {
        return "CoapClient";
    }

    @Override
    public String getScheme() {
        return "coap";
    }

    @Override
    public CoapProtocolClient getClient() {
        return new CoapProtocolClient(
                url -> new CoapClient(url)
                        .setExecutor(executor),
                timeout
        );
    }
}
