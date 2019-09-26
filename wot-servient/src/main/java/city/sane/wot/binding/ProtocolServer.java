package city.sane.wot.binding;

import city.sane.wot.thing.ExposedThing;

import java.net.URI;
import java.util.concurrent.CompletableFuture;

/**
 * A ProtocolServer defines how to expose Thing for interaction via a specific protocol (e.g. HTTP, MQTT, etc.).
 */
public interface ProtocolServer {
    /**
     * Starts the server (e.g. HTTP server) and makes it ready for requests to the exposed things.
     *
     * @return
     */
    CompletableFuture<Void> start();

    /**
     * Stops the server (e.g. HTTP server) and ends the exposure of the Things
     *
     * @return
     */
    CompletableFuture<Void> stop();

    /**
     * Exposes <code>thing</code> and allows interaction with it.
     *
     * @param thing
     *
     * @return
     */
    CompletableFuture<Void> expose(ExposedThing thing);

    /**
     * Stops the exposure of <code>thing</code> and allows no further interaction with the thing.
     *
     * @param thing
     *
     * @return
     */
    CompletableFuture<Void> destroy(ExposedThing thing);

    /**
     * Returns the URL to the Thing Directory if the server supports the listing of all Thing Descriptions.
     *
     * @return
     * @throws ProtocolServerException
     */
    default URI getDirectoryUrl() throws ProtocolServerException {
        throw new ProtocolServerNotImplementedException(getClass(), "directory");
    }

    /**
     * Returns the URL to the thing with the id <code>id</code> if the server supports the listing of certain Thing Descriptions.
     *
     * @param id
     *
     * @return
     * @throws ProtocolServerException
     */
    default URI getThingUrl(String id) throws ProtocolServerException {
        throw new ProtocolServerNotImplementedException(getClass(), "thing-url");
    }
}
