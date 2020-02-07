package city.sane.wot;

import city.sane.Pair;
import city.sane.wot.binding.ProtocolClientFactory;
import city.sane.wot.binding.ProtocolServer;
import com.typesafe.config.Config;
import io.github.classgraph.ClassGraph;
import io.github.classgraph.ClassInfo;
import io.github.classgraph.ScanResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class ServientConfig {
    private static final Logger log = LoggerFactory.getLogger(ServientConfig.class);
    private static final String CONFIG_SERVERS = "wot.servient.servers";
    private static final String CONFIG_CLIENT_FACTORIES = "wot.servient.client-factories";
    private static final String CONFIG_CREDENTIALS = "wot.servient.credentials";
    private static ScanResult scanResult = null;
    private final List<ProtocolServer> servers;
    private final Map<String, ProtocolClientFactory> clientFactories;
    private final Map<String, Object> credentialStore;

    ServientConfig(List<ProtocolServer> servers,
                   Map<String, ProtocolClientFactory> clientFactories,
                   Map<String, Object> credentialStore) {
        this.servers = servers;
        this.clientFactories = clientFactories;
        this.credentialStore = credentialStore;
    }

    public ServientConfig(Config config) throws ServientConfigException {
        List<String> requiredServers;
        if (config.getIsNull(CONFIG_SERVERS)) {
            // search in classpath for available implementations
            log.debug("Config '{}' is set to 'null'. Search in classpath for available servers", CONFIG_SERVERS);
            ScanResult scanResult = scanClasspath();
            requiredServers = scanResult.getClassesImplementing(ProtocolServer.class.getName())
                    .stream().filter(ci -> !ci.hasAnnotation(ServientDiscoveryIgnore.class.getName()))
                    .map(ClassInfo::getName).collect(Collectors.toList());
            log.debug("Found servers: {}", requiredServers);
        }
        else {
            // read servers from config
            requiredServers = config.getStringList(CONFIG_SERVERS);
        }

        servers = new ArrayList<>();
        for (String serverName : requiredServers) {
            ProtocolServer server = initializeServer(config, serverName);
            servers.add(server);
        }

        List<String> requiredFactories;
        if (config.getIsNull(CONFIG_CLIENT_FACTORIES)) {
            // search in classpath for available implementations
            log.debug("Config '{}' is set to 'null'. Search in classpath for available client factories", CONFIG_CLIENT_FACTORIES);
            ScanResult scanResult = scanClasspath();
            requiredFactories = scanResult.getClassesImplementing(ProtocolClientFactory.class.getName())
                    .stream().filter(ci -> !ci.hasAnnotation(ServientDiscoveryIgnore.class.getName()))
                    .map(ClassInfo::getName).collect(Collectors.toList());
            log.debug("Found client factories: {}", requiredFactories);
        }
        else {
            // read client factories from config
            requiredFactories = config.getStringList(CONFIG_CLIENT_FACTORIES);
        }

        clientFactories = new HashMap<>();
        for (String factoryName : requiredFactories) {
            Pair<String, ProtocolClientFactory> pair = initializeClientFactory(config, factoryName);
            clientFactories.put(pair.first(), pair.second());
        }

        // read credentials from config
        credentialStore = new HashMap<>();
        if (config.hasPath(CONFIG_CREDENTIALS)) {
            addCredentials(config.getObject(CONFIG_CREDENTIALS).unwrapped());
        }
    }

    private static ScanResult scanClasspath() {
        if (scanResult == null) {
            scanResult = new ClassGraph().whitelistPackages("city.sane.wot.binding").enableClassInfo().enableAnnotationInfo().scan();
        }
        return scanResult;
    }

    static ProtocolServer initializeServer(Config config,
                                           String serverName) throws ServientConfigException {
        try {
            Class<ProtocolServer> serverKlass = (Class<ProtocolServer>) Class.forName(serverName);
            Constructor<ProtocolServer> constructor;
            ProtocolServer server;
            if (hasConstructor(serverKlass, Config.class)) {
                constructor = serverKlass.getConstructor(Config.class);
                server = constructor.newInstance(config);
            }
            else {
                constructor = serverKlass.getConstructor();
                server = constructor.newInstance();
            }
            return server;
        }
        catch (ClassCastException | ClassNotFoundException | NoSuchMethodException | InstantiationException | IllegalAccessException | InvocationTargetException e) {
            throw new ServientConfigException(e);
        }
    }

    static Pair<String, ProtocolClientFactory> initializeClientFactory(Config config,
                                                                       String factoryName) throws ServientConfigException {
        try {
            Class<ProtocolClientFactory> factoryKlass = (Class<ProtocolClientFactory>) Class.forName(factoryName);
            Constructor<ProtocolClientFactory> constructor;
            ProtocolClientFactory factory;
            if (hasConstructor(factoryKlass, Config.class)) {
                constructor = factoryKlass.getConstructor(Config.class);
                factory = constructor.newInstance(config);
            }
            else {
                constructor = factoryKlass.getConstructor();
                factory = constructor.newInstance();
            }
            return new Pair<>(factory.getScheme(), factory);
        }
        catch (ClassCastException | ClassNotFoundException | NoSuchMethodException | InstantiationException | IllegalAccessException | InvocationTargetException e) {
            throw new ServientConfigException(e);
        }
    }

    /**
     * Stores security credentials (e.g. username and password) for the thing with the id id
     * <code>id</code>.<br> See also: https://www.w3.org/TR/wot-thing-description/#security-serialization-json
     *
     * @param credentials
     */
    private void addCredentials(Map<String, Object> credentials) {
        log.debug("Servient storing credentials for '{}'", credentials.keySet());
        credentialStore.putAll(credentials);
    }

    private static boolean hasConstructor(Class clazz, Class<?>... parameterTypes) {
        try {
            clazz.getConstructor(parameterTypes);
            return true;
        }
        catch (NoSuchMethodException e) {
            return false;
        }
    }

    public List<ProtocolServer> getServers() {
        return servers;
    }

    public Map<String, ProtocolClientFactory> getClientFactories() {
        return clientFactories;
    }

    public Map<String, Object> getCredentialStore() {
        return credentialStore;
    }

    @Override
    public String toString() {
        return "ServientConfig{" +
                "servers=" + servers +
                ", clientFactories=" + clientFactories +
                ", credentialStore=" + credentialStore +
                '}';
    }
}
