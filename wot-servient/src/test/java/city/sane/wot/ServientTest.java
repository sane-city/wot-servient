package city.sane.wot;

import city.sane.wot.binding.*;
import city.sane.wot.content.Content;
import city.sane.wot.thing.ExposedThing;
import city.sane.wot.thing.Thing;
import city.sane.wot.thing.filter.DiscoveryMethod;
import city.sane.wot.thing.filter.ThingFilter;
import city.sane.wot.thing.form.Form;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.junit.Assert.*;

public class ServientTest {
    @Before
    public void setUp() throws Exception {
    }

    @After
    public void tearDown() throws Exception {
    }

    @Test
    public void constructorInstantiatedServer() throws ServientException, ExecutionException, InterruptedException {
        Servient servient = getServientWithServer();

        assertNull(servient.start().get());
        assertNotNull(servient.getServer(MyProtocolServer.class));
    }

    @Test
    public void constructorInstantiatedClientFactory() throws ServientException, ExecutionException, InterruptedException, ProtocolClientException {
        Servient servient = getServientWithClient();

        assertNull(servient.start().get());
        assertNotNull(servient.getClientFor("test"));
    }

    @Test
    public void start() throws ServientException, ExecutionException, InterruptedException {
        Servient servient = getServientWithServer();

        assertNull(servient.start().get());
    }

    @Test
    public void shutdown() throws ServientException, ExecutionException, InterruptedException {
        Servient servient = getServientWithServer();

        assertNull(servient.shutdown().get());
    }

    @Test
    public void expose() throws ServientException, ExecutionException, InterruptedException {
        Servient servient = getServientWithServer();
        servient.addThing(new ExposedThing(servient).setId("counter"));

        assertThat(servient.expose("counter").get(), instanceOf(ExposedThing.class));
    }

    @Test
    public void destroy() throws ServientException, ExecutionException, InterruptedException {
        Servient servient = getServientWithServer();
        servient.addThing(new ExposedThing(servient).setId("counter"));

        assertThat(servient.destroy("counter").get(), instanceOf(ExposedThing.class));
    }

    @Test
    public void fetch() throws ServientException, URISyntaxException, ExecutionException, InterruptedException {
        Servient servient = getServientWithClient();

        assertThat(servient.fetch(new URI("test:/counter")).get(), instanceOf(Thing.class));
    }

    @Test
    public void fetchDirectory() throws ServientException, URISyntaxException, ExecutionException, InterruptedException {
        Servient servient = getServientWithClient();

        assertThat(servient.fetchDirectory(new URI("test:/")).get(), instanceOf(Map.class));
    }

    @Test
    public void fetchDirectoryString() throws ServientException, URISyntaxException, ExecutionException, InterruptedException {
        Servient servient = getServientWithClient();

        assertThat(servient.fetchDirectory("test:/").get(), instanceOf(Map.class));
    }

    @Test
    public void discover() throws ServientException, ExecutionException, InterruptedException {
        Servient servient = getServientWithClient();

        assertThat(servient.discover().get(), instanceOf(Collection.class));
    }

    @Test
    public void discoverLocal() throws ServientException, ExecutionException, InterruptedException {
        Servient servient = getServientWithClient();

        ThingFilter filter = new ThingFilter().setMethod(DiscoveryMethod.LOCAL);
        assertThat(servient.discover(filter).get(), instanceOf(Collection.class));
    }

    @Test
    public void discoverDirectory() throws ServientException, ExecutionException, InterruptedException, URISyntaxException {
        Servient servient = getServientWithClient();

        ThingFilter filter = new ThingFilter().setMethod(DiscoveryMethod.DIRECTORY).setUrl(new URI("test:/"));
        assertThat(servient.discover(filter).get(), instanceOf(Collection.class));
    }

    @Test(expected = ProtocolClientNotImplementedException.class)
    public void discoverWithNoClientImplementsDiscover() throws Throwable {
        Servient servient = getServientWithNoClient();

        try {
            assertThat(servient.discover().get(), instanceOf(Collection.class));
        }
        catch (ExecutionException e) {
            throw e.getCause();
        }
    }

    @Test(expected = ServientException.class)
    public void runScriptWithNoEngine() throws ServientException {
        Servient servient = new Servient();
        servient.runScript(new File("foo.bar"),null);
    }

    @Test
    public void getAddresses() {
        Servient.getAddresses();

        // should not fail
        assertTrue(true);
    }

    @Test
    public void clientOnly() throws ServientException {
        Config config = ConfigFactory
                .parseString("wot.servient.servers = [\"" + MyProtocolServer.class.getName() + "\"]")
                .withFallback(ConfigFactory.load());
        Servient servient = Servient.clientOnly(config);

        assertTrue(servient.getServers().isEmpty());
    }

    private Servient getServientWithServer() throws ServientException {
        Config config = ConfigFactory
                .parseString("wot.servient.servers = [\"" + MyProtocolServer.class.getName() + "\"]")
                .withFallback(ConfigFactory.load());
        return new Servient(config);
    }

    private Servient getServientWithClient() throws ServientException {
        Config config = ConfigFactory
                .parseString("wot.servient.client-factories = [\"" + MyProtocolClientFactory.class.getName() + "\"]")
                .withFallback(ConfigFactory.load());
        return new Servient(config);
    }

    private Servient getServientWithNoClient() throws ServientException {
        Config config = ConfigFactory
                .parseString("wot.servient.client-factories = []")
                .withFallback(ConfigFactory.load());
        return new Servient(config);
    }

    @Test
    public void addThingWithoutId() throws ServientException {
        Servient servient = new Servient();
        ExposedThing thing = new ExposedThing(servient);
        servient.addThing(thing);

        assertNotNull(thing.getId());
    }

    static class MyProtocolServer implements ProtocolServer {
        public MyProtocolServer(Config config) {

        }

        @Override
        public CompletableFuture<Void> start() {
            return CompletableFuture.completedFuture(null);
        }

        @Override
        public CompletableFuture<Void> stop() {
            return CompletableFuture.completedFuture(null);
        }

        @Override
        public CompletableFuture<Void> expose(ExposedThing thing) {
            return CompletableFuture.completedFuture(null);
        }

        @Override
        public CompletableFuture<Void> destroy(ExposedThing thing) {
            return CompletableFuture.completedFuture(null);
        }
    }

    static class MyProtocolClientFactory implements ProtocolClientFactory {
        public MyProtocolClientFactory(Config config) {

        }

        @Override
        public String getScheme() {
            return "test";
        }

        @Override
        public ProtocolClient getClient() throws ProtocolClientException {
            return new MyProtocolClient();
        }
    }

    static class MyProtocolClient implements ProtocolClient {
        @Override
        public CompletableFuture<Content> readResource(Form form) {
            String json = null;
            switch (form.getHref()) {
                case "test:/":
                    json = "{\"counter\": {\"id\": \"counter\"}}";
                    break;

                case "test:/counter":
                    json = "{\"id\": \"counter\"}";
                    break;
            }
            return CompletableFuture.completedFuture(new Content("application/json", json.getBytes()));
        }

        @Override
        public CompletableFuture<Collection<Thing>> discover(ThingFilter filter) {
            return CompletableFuture.completedFuture(Collections.emptyList());
        }
    }
}