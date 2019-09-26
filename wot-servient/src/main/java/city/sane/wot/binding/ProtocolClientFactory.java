package city.sane.wot.binding;

import java.util.concurrent.CompletableFuture;

/**
 * A ProtocolClientFactory is responsible for creating new {@link ProtocolClient} instances. There is a separate client instance for each
 * {@link city.sane.wot.thing.ConsumedThing}.
 */
public interface ProtocolClientFactory {
    String getScheme();

    ProtocolClient getClient() throws ProtocolClientException;

    /**
     * Is called on servient start.
     *
     * @return
     */
    default CompletableFuture<Void> init() {
        return CompletableFuture.completedFuture(null);
    }

    /**
     * Is called on servient shutdown.
     *
     * @return
     */
    default CompletableFuture<Void> destroy() {
        return CompletableFuture.completedFuture(null);
    }
}
