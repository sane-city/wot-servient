package city.sane.wot.binding;

import city.sane.wot.Servient;
import city.sane.wot.thing.ExposedThing;
import org.junit.Before;
import org.junit.Test;

import java.util.concurrent.CompletableFuture;

public class ProtocolServerTest {
    private String id;

    @Before
    public void setUp() {
        id = "counter";
    }

    @Test(expected = ProtocolServerNotImplementedException.class)
    public void getDirectoryUrl() throws ProtocolServerException {
        new MyProtocolServer().getDirectoryUrl();
    }

    @Test(expected = ProtocolServerNotImplementedException.class)
    public void getThingUrl() throws ProtocolServerException {
        new MyProtocolServer().getThingUrl(id);
    }

    class MyProtocolServer implements ProtocolServer {
        /**
         * Starts the server (e.g. HTTP server) and makes it ready for requests to the exposed
         * things.
         *
         * @return
         * @param servient
         */
        @Override
        public CompletableFuture<Void> start(Servient servient) {
            return null;
        }

        /**
         * Stops the server (e.g. HTTP server) and ends the exposure of the Things
         *
         * @return
         */
        @Override
        public CompletableFuture<Void> stop() {
            return null;
        }

        /**
         * Exposes <code>thing</code> and allows interaction with it.
         *
         * @param thing
         * @return
         */
        @Override
        public CompletableFuture<Void> expose(ExposedThing thing) {
            return null;
        }

        /**
         * Stops the exposure of <code>thing</code> and allows no further interaction with the
         * thing.
         *
         * @param thing
         * @return
         */
        @Override
        public CompletableFuture<Void> destroy(ExposedThing thing) {
            return null;
        }
    }
}