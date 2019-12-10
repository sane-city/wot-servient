package city.sane.wot;

import city.sane.wot.binding.*;
import city.sane.wot.content.Content;
import city.sane.wot.thing.ExposedThing;
import city.sane.wot.thing.Thing;
import city.sane.wot.thing.filter.DiscoveryMethod;
import city.sane.wot.thing.filter.SparqlThingQuery;
import city.sane.wot.thing.filter.ThingFilter;
import city.sane.wot.thing.filter.ThingQueryException;
import city.sane.wot.thing.form.Form;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import com.typesafe.config.ConfigValue;
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

    @Test(expected = ServientException.class)
    public void constructorBadServerWithoutImplementation() throws ServientException {
        Config config = ConfigFactory
                .parseString("wot.servient.servers = [\"" + MyBadMissingImplementationProtocolServer.class.getName() + "\"]")
                .withFallback(ConfigFactory.load());
        new Servient(config);
    }

    @Test(expected = ServientException.class)
    public void constructorBadServerMissingConstructor() throws ServientException {
        Config config = ConfigFactory
                .parseString("wot.servient.servers = [\"" + MyBadMissingConstructorProtocolServer.class.getName() + "\"]")
                .withFallback(ConfigFactory.load());
        new Servient(config);
    }

    @Test(expected = ServientException.class)
    public void constructorBadClientWithoutImplementation() throws ServientException {
        Config config = ConfigFactory
                .parseString("wot.servient.client-factories = [\"" + MyBadMissingImplementationProtocolClientFactory.class.getName() + "\"]")
                .withFallback(ConfigFactory.load());
        new Servient(config);
    }

    @Test(expected = ServientException.class)
    public void constructorBadClientMissingConstructor() throws ServientException {
        Config config = ConfigFactory
                .parseString("wot.servient.client-factories = [\"" + MyBadMissingConstructorProtocolClientFactory.class.getName() + "\"]")
                .withFallback(ConfigFactory.load());
        new Servient(config);
    }

    @Test
    public void constructorWithCredentials() throws ServientException {
        Config config = ConfigFactory
                .parseString("wot.servient.credentials { \"counter\" = \"mySecret\" }")
                .withFallback(ConfigFactory.load());
        Servient servient = new Servient(config);

        assertEquals("mySecret",
                ((ConfigValue) servient.getCredentials("counter")).unwrapped());
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

    @Test(expected = ServientException.class)
    public void exposeWithoutServers() throws Throwable {
        Servient servient = getServientWithNoServer();
        servient.addThing(new ExposedThing(servient).setId("counter"));

        try {
            servient.expose("counter").get();
        }
        catch (ExecutionException e) {
            throw e.getCause();
        }
    }

    @Test(expected = ServientException.class)
    public void exposeUnknownThing() throws Throwable {
        Servient servient = getServientWithServer();

        try {
            servient.expose("counter").get();
        }
        catch (ExecutionException e) {
            throw e.getCause();
        }
    }

    @Test
    public void destroy() throws ServientException, ExecutionException, InterruptedException {
        Servient servient = getServientWithServer();
        servient.addThing(new ExposedThing(servient).setId("counter"));

        assertThat(servient.destroy("counter").get(), instanceOf(ExposedThing.class));
    }

    @Test(expected = ServientException.class)
    public void destroyWithoutServers() throws Throwable {
        Servient servient = getServientWithNoServer();
        servient.addThing(new ExposedThing(servient).setId("counter"));

        try {
            servient.destroy("counter").get();
        }
        catch (ExecutionException e) {
            throw e.getCause();
        }
    }

    @Test
    public void fetch() throws ServientException, URISyntaxException, ExecutionException, InterruptedException {
        Servient servient = getServientWithClient();

        assertThat(servient.fetch("test:/counter").get(), instanceOf(Thing.class));
    }

    @Test(expected = ServientException.class)
    public void fetchMissingScheme() throws Throwable {
        Servient servient = getServientWithNoClient();

        try {
            servient.fetch("test:/counter").get();
        }
        catch (ExecutionException e) {
            throw e.getCause();
        }
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
    public void discoverWithQuery() throws ServientException, ExecutionException, InterruptedException, ThingQueryException {
        Servient servient = getServientWithClient();

        ThingFilter filter = new ThingFilter();
        filter.setQuery(new SparqlThingQuery("?x <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <https://www.w3.org/2019/wot/td##Thing> ."));
        assertThat(servient.discover(filter).get(), instanceOf(Collection.class));
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
            servient.discover().get();
        }
        catch (ExecutionException e) {
            throw e.getCause();
        }
    }

    @Test(expected = ServientException.class)
    public void runScriptWithNoEngine() throws ServientException {
        Servient servient = new Servient();
        servient.runScript(new File("foo.bar"), null);
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

    private Servient getServientWithNoServer() throws ServientException {
        Config config = ConfigFactory
                .parseString("wot.servient.servers = []")
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

    @Test
    public void getClientForNegative() throws ProtocolClientException, ServientException {
        Servient servient = new Servient();

        assertNull(servient.getClientFor("test"));
    }

    @Test(expected = ServientException.class)
    public void register() throws Throwable {
        Servient servient = new Servient();

        try {
            servient.register("test://foo/bar", null).get();
        }
        catch (ExecutionException e) {
            throw e.getCause();
        }
    }

    @Test(expected = ServientException.class)
    public void unregister() throws Throwable {
        Servient servient = new Servient();

        try {
            servient.unregister("test://foo/bar", null).get();
        }
        catch (ExecutionException e) {
            throw e.getCause();
        }
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

    static class MyBadMissingImplementationProtocolServer {
        public MyBadMissingImplementationProtocolServer(Config config) {

        }
    }

    static class MyBadMissingConstructorProtocolServer implements ProtocolServer {
        /**
         * Starts the server (e.g. HTTP server) and makes it ready for requests to the exposed things.
         *
         * @return
         */
        @Override
        public CompletableFuture<Void> start() {
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
         *
         * @return
         */
        @Override
        public CompletableFuture<Void> expose(ExposedThing thing) {
            return null;
        }

        /**
         * Stops the exposure of <code>thing</code> and allows no further interaction with the thing.
         *
         * @param thing
         *
         * @return
         */
        @Override
        public CompletableFuture<Void> destroy(ExposedThing thing) {
            return null;
        }
    }

    static class MyBadMissingImplementationProtocolClientFactory {
        public MyBadMissingImplementationProtocolClientFactory(Config config) {

        }
    }

    static class MyBadMissingConstructorProtocolClientFactory implements ProtocolClientFactory {
        @Override
        public String getScheme() {
            return null;
        }

        @Override
        public ProtocolClient getClient() {
            return null;
        }
    }
}