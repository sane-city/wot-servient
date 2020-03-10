package city.sane.wot.binding.coap;

import city.sane.wot.binding.ProtocolClientFactory;
import org.slf4j.bridge.SLF4JBridgeHandler;

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

    public CoapProtocolClientFactory() {
        executor = Executors.newFixedThreadPool(10);
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
        return new CoapProtocolClient(executor, timeout);
    }
}
