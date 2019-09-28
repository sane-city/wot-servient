package city.sane.wot;

import city.sane.wot.binding.*;
import city.sane.wot.content.ContentCodecException;
import city.sane.wot.content.ContentManager;
import city.sane.wot.scripting.ScriptingManager;
import city.sane.wot.scripting.ScriptingManagerException;
import city.sane.wot.thing.ExposedThing;
import city.sane.wot.thing.Thing;
import city.sane.wot.thing.action.ExposedThingAction;
import city.sane.wot.thing.event.ExposedThingEvent;
import city.sane.wot.thing.filter.DiscoveryMethod;
import city.sane.wot.thing.filter.ThingFilter;
import city.sane.wot.thing.form.Form;
import city.sane.wot.thing.property.ExposedThingProperty;
import city.sane.wot.thing.schema.ObjectSchema;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.net.*;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * The Servient hosts, exposes and consumes things based on provided protocol bindings.
 * https://w3c.github.io/wot-architecture/#sec-servient-implementation<br>
 * It reads the servers contained in the configuration parameter "wot.servient.servers", starts them and thus exposes Things via the protocols supported by the
 * servers. "wot.servient.servers" should contain an array of strings of fully qualified class names implementing {@link ProtocolServer}.<br>
 * It also reads the clients contained in the configuration parameter "wot.servient.client-factories" and is then able to consume Things via the protocols
 * supported by the clients. "wot.servient.client-factories" should contain an array of strings of fully qualified class names implementing
 * {@link ProtocolClientFactory}.<br>
 * The optional configuration parameter "wot.servient.credentials" can contain credentials (e.g. username and password) for the different things.  The parameter
 * should contain a map which uses the thing ids as key.
 */
public class Servient {
    final static Logger log = LoggerFactory.getLogger(Servient.class);

    private final Config config;
    private final List<ProtocolServer> servers = new ArrayList<>();
    private final Map<String, ProtocolClientFactory> clientFactories = new HashMap<>();
    private final Map<String, ExposedThing> things = new HashMap<>();
    private Map<String, Object> credentialStore = new HashMap<>();

    /**
     * Creates a servient with the given <code>config</code>.
     *
     * @param config
     */
    public Servient(Config config) {
        this.config = config;

        // read servers from config
        List<String> requiredServers = this.config.getStringList("wot.servient.servers");
        requiredServers.forEach(serverName -> {
            try {
                Class<ProtocolServer> serverKlass = (Class<ProtocolServer>) Class.forName(serverName);
                Constructor<ProtocolServer> constructor = serverKlass.getConstructor(Config.class);
                ProtocolServer server = constructor.newInstance(config);
                servers.add(server);
            }
            catch (ClassNotFoundException | NoSuchMethodException | InstantiationException | IllegalAccessException | InvocationTargetException e) {
                throw new RuntimeException(e);
            }
        });

        // read client factories from config
        List<String> requiredFactories = this.config.getStringList("wot.servient.client-factories");
        requiredFactories.forEach(factoryName -> {
            try {
                Class<ProtocolClientFactory> factoryKlass = (Class<ProtocolClientFactory>) Class.forName(factoryName);
                Constructor<ProtocolClientFactory> constructor = factoryKlass.getConstructor(Config.class);
                ProtocolClientFactory factory = constructor.newInstance(config);
                clientFactories.put(factory.getScheme(), factory);
            }
            catch (ClassNotFoundException | NoSuchMethodException | InstantiationException | IllegalAccessException | InvocationTargetException e) {
                throw new RuntimeException(e);
            }
        });

        // read credentials from config
        String credentialsPath = "wot.servient.credentials";
        if (this.config.hasPath(credentialsPath)) {
            addCredentials(this.config.getObject(credentialsPath));
        }
    }

    /**
     * Creates a servient.
     */
    public Servient() {
        this(ConfigFactory.load());
    }

    @Override
    public String toString() {
        return "Servient [servers=" + getServers() + " clientFactories=" + clientFactories.values() + "]";
    }

    /**
     * Returns a list of all servers supported by the servient.
     *
     * @return
     */
    public List<ProtocolServer> getServers() {
        return servers;
    }

    /**
     * Launch the servient. All servers supported by the servient (e.g. HTTP, CoAP, ...) are started. The servers are then ready to accept requests for the
     * exposed Things.
     *
     * @return
     */
    public CompletableFuture<Void> start() {
        CompletableFuture<Void>[] serverFutures = servers.stream().map(ProtocolServer::start).toArray(CompletableFuture[]::new);
        CompletableFuture<Void>[] clientFutures = clientFactories.values().stream().map(ProtocolClientFactory::init).toArray(CompletableFuture[]::new);

        CompletableFuture<Void>[] futures = Stream.concat(Arrays.stream(clientFutures), Arrays.stream(serverFutures)).toArray(CompletableFuture[]::new);

        return CompletableFuture.allOf(futures);
    }

    /**
     * Shut down the servient. All servers supported by the servient (e.g. HTTP, CoAP, ...) are shut down. Interaction with exposed Things is then no longer
     * possible.
     *
     * @return
     */
    public CompletableFuture<Void> shutdown() {
        CompletableFuture<Void>[] clientFutures = clientFactories.values().stream().map(ProtocolClientFactory::destroy).toArray(CompletableFuture[]::new);
        CompletableFuture<Void>[] serverFutures = servers.stream().map(ProtocolServer::stop).toArray(CompletableFuture[]::new);

        CompletableFuture<Void>[] futures = Stream.concat(Arrays.stream(clientFutures), Arrays.stream(serverFutures)).toArray(CompletableFuture[]::new);

        return CompletableFuture.allOf(futures);
    }

    /**
     * All servers supported by Servient are instructed to expose the Thing with the given <code>id</code>. Then it is possible to interact with the Thing via
     * different protocols (e.g. HTTP, CoAP, ...). Before a thing can be exposed, it must be added via {@link #addThing}.
     *
     * @param id
     *
     * @return
     */
    public CompletableFuture<ExposedThing> expose(String id) {
        ExposedThing thing = things.get(id);

        if (servers.isEmpty()) {
            log.warn("Servient has no servers to expose Things");
            return CompletableFuture.completedFuture(thing);
        }

        if (thing == null) {
            log.warn("Thing must be added to the servient first");
            return CompletableFuture.completedFuture(thing);
        }

        log.info("Servient exposing '{}'", id);

        // initializing forms
        Map<String, ExposedThingProperty> properties = thing.getProperties();
        properties.forEach((n, p) -> p.setForms(new ArrayList<>()));
        Map<String, ExposedThingAction> actions = thing.getActions();
        actions.forEach((n, a) -> a.setForms(new ArrayList<>()));
        Map<String, ExposedThingEvent> events = thing.getEvents();
        events.forEach((n, e) -> e.setForms(new ArrayList<>()));

        CompletableFuture<Void>[] serverFutures = getServers().stream().map(s -> s.expose(thing)).toArray(CompletableFuture[]::new);
        return CompletableFuture.allOf(serverFutures).thenApply((result) -> thing);
    }

    /**
     * All servers supported by Servient are instructed not to expose the Thing with the given <code>id</code> any longer. After that it is not possible to
     * interact with the Thing via the different protocols (e.g. HTTP, CoAP, ...).
     *
     * @param id
     *
     * @return
     */
    public CompletableFuture<ExposedThing> destroy(String id) {
        ExposedThing thing = things.get(id);

        if (servers.isEmpty()) {
            log.warn("Servient has no servers to stop exposure Things");
            return CompletableFuture.completedFuture(thing);
        }

        log.info("Servient stop exposing '{}'", thing);

        // reset forms
        Map<String, ExposedThingProperty> properties = thing.getProperties();
        properties.forEach((n, p) -> p.setForms(new ArrayList<>()));
        Map<String, ExposedThingAction> actions = thing.getActions();
        actions.forEach((n, a) -> a.setForms(new ArrayList<>()));
        Map<String, ExposedThingEvent> events = thing.getEvents();
        events.forEach((n, e) -> e.setForms(new ArrayList<>()));

        CompletableFuture<Void>[] serverFutures = getServers().stream().map(s -> s.destroy(thing)).toArray(CompletableFuture[]::new);
        return CompletableFuture.allOf(serverFutures).thenApply((result) -> thing);
    }

    /**
     * Adds <code>thing</code> to the servient. This allows the Thing to be exposed later.
     *
     * @param exposedThing
     *
     * @return
     */
    public boolean addThing(ExposedThing exposedThing) {
        if (exposedThing.getId() == null || exposedThing.getId().isEmpty()) {
            log.warn("Servient generating ID for '{}'", exposedThing);
            exposedThing.setId(Thing.randomId());
        }

        ExposedThing previous = things.putIfAbsent(exposedThing.getId(), exposedThing);
        return previous == null;
    }

    /**
     * Searches for the matching {@link ProtocolClient} for <code>scheme</code> (e.g. http, coap, mqtt, etc.). If no client can be found, <code>null</code> is
     * returned.
     *
     * @param scheme
     *
     * @return
     * @throws ProtocolClientException
     */
    public ProtocolClient getClientFor(String scheme) throws ProtocolClientException {
        ProtocolClientFactory factory = clientFactories.get(scheme);
        if (factory != null) {
            return factory.getClient();
        }
        else {
            log.warn("Servient has no ClientFactory for scheme '" + scheme + "'");
            return null;
        }
    }

    /**
     * Returns all things that have been added to the servient.
     *
     * @return
     */
    public Map<String, ExposedThing> getThings() {
        return things;
    }

    /**
     * Calls <code>url</code> and expects a Thing Description there. Returns the description as a {@link Thing}.
     *
     * @param url
     *
     * @return
     */
    public CompletableFuture<Thing> fetch(URI url) {
        log.debug("Fetch thing from url '{}'", url);
        String scheme = url.getScheme();

        try {
            ProtocolClient client = getClientFor(scheme);
            if (client != null) {
                Form form = new Form.Builder()
                        .setHref(url.toString())
                        .build();
                return client.readResource(form).thenApply((content) -> {
                    try {
                        Map map = ContentManager.contentToValue(content, new ObjectSchema());
                        Thing thing = Thing.fromMap(map);
                        return thing;
                    }
                    catch (ContentCodecException e) {
                        e.printStackTrace();
                        throw new CompletionException(new ServientException("Error while fetching TD: " + e.toString()));
                    }
                });

            }
            else {
                return CompletableFuture.failedFuture(new ServientException("Unable to fetch '" + url + "'. Missing ClientFactory for scheme '" + scheme + "'"));
            }
        }
        catch (ProtocolClientException e) {
            return CompletableFuture.failedFuture(new ServientException("Unable to create client: " + e.getMessage()));
        }
    }

    /**
     * Calls <code>url</code> and expects a Thing Description there. Returns the description as a {@link Thing}.
     *
     * @param url
     *
     * @return
     */
    public CompletableFuture<Thing> fetch(String url) throws URISyntaxException {
        return fetch(new URI(url));
    }

    /**
     * Calls <code>url</code> and expects a Thing Directory there. Returns a list with all found {@link Thing}.
     *
     * @param url
     *
     * @return
     */
    public CompletableFuture<Map<String, Thing>> fetchDirectory(URI url) {
        log.debug("Fetch thing directory from url '{}'", url);
        String scheme = url.getScheme();

        try {
            ProtocolClient client = getClientFor(scheme);
            if (client != null) {
                Form form = new Form.Builder()
                        .setHref(url.toString())
                        .build();
                return client.readResource(form).thenApply(content -> {
                    try {
                        Map<String, Map> value = ContentManager.contentToValue(content, new ObjectSchema());

                        Map<String, Thing> things = new HashMap<>();
                        for (Map.Entry<String, Map> entry : value.entrySet()) {
                            String id = entry.getKey();
                            Map map = entry.getValue();
                            Thing thing = Thing.fromMap(map);

                            things.put(id, thing);
                        }

                        return things;
                    }
                    catch (ContentCodecException e2) {
                        throw new CompletionException(new ServientException("Error while fetching TD directory: " + e2.toString()));
                    }
                });

            }
            else {
                return CompletableFuture.failedFuture(new ServientException("Unable to fetch directory '" + url + "'. Missing ClientFactory for scheme '" + scheme + "'"));
            }
        }
        catch (ProtocolClientException e) {
            return CompletableFuture.failedFuture(new ServientException("Unable to create client: " + e.getMessage()));
        }
    }

    /**
     * Calls <code>url</code> and expects a Thing Directory there. Returns a list with all found {@link Thing}.
     *
     * @param url
     *
     * @return
     * @throws URISyntaxException
     */
    public CompletableFuture<Map<String, Thing>> fetchDirectory(String url) throws URISyntaxException {
        return fetchDirectory(new URI(url));

    }

    /**
     * Adds <code>thing</code> to the Thing Directory <code>directory</code>.
     *
     * @param directory
     * @param thing
     *
     * @return
     */
    public CompletableFuture<Void> register(URI directory, ExposedThing thing) {
        // FIXME: implement
        return CompletableFuture.failedFuture(new RuntimeException("not implemented"));
    }

    /**
     * Adds <code>thing</code> to the Thing Directory <code>directory</code>.
     *
     * @param directory
     * @param thing
     *
     * @return
     * @throws URISyntaxException
     */
    public CompletableFuture<Void> register(String directory, ExposedThing thing) throws URISyntaxException {
        return register(new URI(directory), thing);
    }

    /**
     * Removes <code>thing</code> from Thing Directory <code>directory</code>.
     *
     * @param directory
     * @param thing
     *
     * @return
     */
    public CompletableFuture<Void> unregister(URI directory, ExposedThing thing) {
        // FIXME: implement
        return CompletableFuture.failedFuture(new RuntimeException("not implemented"));
    }

    /**
     * Removes <code>thing</code> from Thing Directory <code>directory</code>.
     *
     * @param directory
     * @param thing
     *
     * @return
     * @throws URISyntaxException
     */
    public CompletableFuture<Void> unregister(String directory, ExposedThing thing) throws URISyntaxException {
        return unregister(new URI(directory), thing);
    }

    /**
     * Starts a discovery process and searches for the things defined in <code>filter</code>.
     * Not all {@link ProtocolClient} implementations support discovery. If none of the available clients support discovery, a
     * {@link ProtocolClientNotImplementedException} will be thrown.
     *
     * @param filter
     *
     * @return
     */
    public CompletableFuture<Collection<Thing>> discover(ThingFilter filter) {
        if (filter.getMethod() == DiscoveryMethod.DIRECTORY) {
            return fetchDirectory(filter.getUrl()).thenApply(Map::values);
        }
        else {
            Set<Thing> things = new HashSet<>();
            AtomicBoolean atLeastOneImplementation = new AtomicBoolean(false);
            List<CompletableFuture> discoverFutures = new ArrayList<>();

            // get local Things with or without filter query
            if (filter.getQuery() != null) {
                things.addAll(filter.getQuery().filter(getThings().values().stream().map(Thing.class::cast).collect(Collectors.toList())));
            }
            else {
                things.addAll(getThings().values().stream().map(Thing.class::cast).collect(Collectors.toList()));
            }

            if (filter.getMethod() == DiscoveryMethod.LOCAL) {
                CompletableFuture<Collection<Thing>> future = new CompletableFuture<>();
                future.complete(things);
                return future;
            }

            for (ProtocolClientFactory factory : clientFactories.values()) {
                try {
                    ProtocolClient client = factory.getClient();
                    CompletableFuture<Void> future = client.discover(filter).handle((clientThings, e) -> {
                        if (e == null) {
                            things.addAll(clientThings);
                            atLeastOneImplementation.set(true);
                        }
                        else if (!(e instanceof ProtocolClientNotImplementedException)) {
                            throw new CompletionException(e);
                        }
                        return null;
                    });
                    discoverFutures.add(future);

                }
                catch (ProtocolClientException e) {
                    return CompletableFuture.failedFuture(e);
                }
            }

            CompletableFuture<Void> discoveryDone = CompletableFuture
                    .allOf(discoverFutures.toArray(CompletableFuture[]::new));
            return discoveryDone.handle((r, e) -> {
                if (e == null) {
                    if (atLeastOneImplementation.get()) {
                        return things;
                    }
                    else {
                        throw new CompletionException(new ProtocolClientNotImplementedException("None of the available clients implements 'discovery'"));
                    }
                }
                else {
                    throw new CompletionException(e);
                }
            });
        }
    }

    /**
     * Starts a discovery process for all available Things.
     * Not all {@link ProtocolClient} implementations support discovery. If none of the available clients support discovery, a
     * {@link ProtocolClientNotImplementedException} will be thrown.
     *
     * @return
     */
    public CompletableFuture<Collection<Thing>> discover() {
        return discover(new ThingFilter(DiscoveryMethod.ANY));
    }

    /**
     * Returns the server of type <code>server</code>. If the serving does not support this type, <code>null</code> is returned.
     *
     * @param server
     * @param <T>
     *
     * @return
     */
    public <T extends ProtocolServer> T getServer(Class<T> server) {
        Optional<T> optional = (Optional<T>) servers.stream().filter(server::isInstance).findFirst();
        if (optional.isPresent()) {
            return optional.get();
        }
        else {
            return null;
        }
    }

    /**
     * Stores security credentials (e.g. username and password) for the thing with the id id <code>id</code>.<br>
     * See also: https://www.w3.org/TR/wot-thing-description/#security-serialization-json
     *
     * @param credentials
     */
    public void addCredentials(Map credentials) {
        log.debug("Servient storing credentials for '{}'", credentials.keySet());
        credentialStore.putAll(credentials);
    }

    /**
     * Returns the security credentials (e.g. username and password) for the thing with the id <code>id</code>.<br>
     * See also: https://www.w3.org/TR/wot-thing-description/#security-serialization-json
     *
     * @param id
     *
     * @return
     */
    public Object getCredentials(String id) {
        log.debug("Servient looking up credentials for '{}'", id);
        return credentialStore.get(id);
    }

    /**
     * Executes the WoT script in <code>file</code> and passes <code>wot</code> to the script as WoT object.<br>
     * Only the script languages known to the {@link ScriptingManager} are supported.
     *
     * @param file
     * @param wot
     * @throws ServientException
     */
    public void runScript(File file, Wot wot) throws ServientException {
        try {
            ScriptingManager.runScript(file, wot);
        }
        catch (ScriptingManagerException e) {
            throw new ServientException(e);
        }
    }

    /**
     * Returns a list of the IP addresses of all network interfaces of the local computer. If no IP addresses can be obtained, 127.0.0.1 is returned.
     *
     * @return
     */
    public static Set<String> getAddresses() {
        try {
            Set<String> addresses = new HashSet<>();

            Enumeration<NetworkInterface> ifaces = NetworkInterface.getNetworkInterfaces();
            while (ifaces.hasMoreElements()) {
                NetworkInterface iface = ifaces.nextElement();

                if (!iface.isUp() || iface.isLoopback() || iface.isPointToPoint()) {
                    continue;
                }

                Enumeration<InetAddress> ifaceAddresses = iface.getInetAddresses();
                while (ifaceAddresses.hasMoreElements()) {
                    InetAddress ifaceAddress = ifaceAddresses.nextElement();

                    if (ifaceAddress.isLoopbackAddress() || ifaceAddress.isLinkLocalAddress() || ifaceAddress.isMulticastAddress()) {
                        continue;
                    }

                    if (ifaceAddress instanceof Inet4Address) {
                        String hostAddress = ifaceAddress.getHostAddress();
                        addresses.add(hostAddress);
                    }
                    else if (ifaceAddress instanceof Inet6Address) {
                        String hostAddress = ifaceAddress.getHostAddress();

                        // remove scope
                        int percent = hostAddress.indexOf("%");
                        if (percent != -1) {
                            hostAddress = hostAddress.substring(0, percent);
                        }

                        addresses.add("[" + hostAddress + "]");
                    }
                }
            }

            return addresses;
        }
        catch (SocketException e) {
            return new HashSet<>(Arrays.asList("127.0.0.1"));
        }
    }

    /**
     * Creates a {@link Servient} with the given <code>config</code>. The servient will not start any servers and can therefore only consume things
     * and not expose any things.
     *
     * @param config
     */
    public static Servient clientOnly(Config config) {
        Config clientOnlyConfig = ConfigFactory
                .parseString("wot.servient.servers = []")
                .withFallback(config);
        return new Servient(clientOnlyConfig);
    }
}
