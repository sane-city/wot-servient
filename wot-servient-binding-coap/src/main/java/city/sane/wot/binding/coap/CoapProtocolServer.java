package city.sane.wot.binding.coap;

import city.sane.wot.Servient;
import city.sane.wot.binding.ProtocolServer;
import city.sane.wot.binding.ProtocolServerException;
import city.sane.wot.binding.coap.resource.*;
import city.sane.wot.content.ContentManager;
import city.sane.wot.thing.ExposedThing;
import city.sane.wot.thing.action.ExposedThingAction;
import city.sane.wot.thing.event.ExposedThingEvent;
import city.sane.wot.thing.form.Form;
import city.sane.wot.thing.form.Operation;
import city.sane.wot.thing.property.ExposedThingProperty;
import com.typesafe.config.Config;
import org.eclipse.californium.core.CoapResource;
import org.eclipse.californium.core.server.resources.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.bridge.SLF4JBridgeHandler;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.URI;
import java.net.URISyntaxException;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.*;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import static java.util.concurrent.CompletableFuture.*;

/**
 * Allows exposing Things via CoAP.
 */
public class CoapProtocolServer implements ProtocolServer {
    private static final Logger log = LoggerFactory.getLogger(CoapProtocolServer.class);

    static {
        // Californium uses java.util.logging. We need to redirect all log messages to logback
        SLF4JBridgeHandler.removeHandlersForRootLogger();
        SLF4JBridgeHandler.install();
    }

    private final String bindHost;
    private final int bindPort;
    private final List<String> addresses;
    private final Map<String, ExposedThing> things;
    private final Map<String, CoapResource> resources;
    private final Supplier<WotCoapServer> serverSupplier;
    private WotCoapServer server;

    public CoapProtocolServer(Config config) {
        bindHost = "0.0.0.0";
        bindPort = config.getInt("wot.servient.coap.bind-port");
        if (!config.getStringList("wot.servient.coap.addresses").isEmpty()) {
            addresses = config.getStringList("wot.servient.coap.addresses");
        }
        else {
            addresses = Servient.getAddresses().stream().map(a -> "coap://" + a + ":" + bindPort).collect(Collectors.toList());
        }
        things = new HashMap<>();
        resources = new HashMap<>();
        serverSupplier = () -> new WotCoapServer(this);
    }

    CoapProtocolServer(String bindHost,
                       int bindPort,
                       List<String> addresses,
                       Map<String, ExposedThing> things,
                       Map<String, CoapResource> resources,
                       Supplier<WotCoapServer> serverSupplier,
                       WotCoapServer server) {
        this.bindHost = bindHost;
        this.bindPort = bindPort;
        this.addresses = addresses;
        this.things = things;
        this.resources = resources;
        this.serverSupplier = serverSupplier;
        this.server = server;
    }

    @Override
    public String toString() {
        return "WotCoapServer";
    }

    @Override
    public CompletableFuture<Void> start(Servient servient) {
        log.info("Starting on '{}' port '{}'", bindHost, bindPort);

        if (server != null) {
            return completedFuture(null);
        }

        return runAsync(() -> {
            server = serverSupplier.get();
            server.start();
        });
    }

    @Override
    public CompletableFuture<Void> stop() {
        log.info("Stopping on '{}' port '{}'", bindHost, bindPort);

        if (server == null) {
            return completedFuture(null);
        }

        return runAsync(() -> {
            server.stop();
            server.destroy();

            try {
                // TODO: Wait some time after the server has shut down. Apparently the CoAP server reports too early that it was terminated, even though the
                //  port is still in use. This sometimes led to errors during the tests because other CoAP servers were not able to be started because the port
                //  was already in use. This error only occurred in the GitLab CI (in Docker). Instead of waiting, the error should be reported to the
                //  maintainer of the CoAP server and fixed. Because the isolation of the error is so complex, this workaround was chosen.
                waitForPort(bindPort);
            }
            catch (TimeoutException e) {
                throw new CompletionException(e);
            }

            log.debug("Server stopped");
        });
    }

    @Override
    public CompletableFuture<Void> expose(ExposedThing thing) {
        log.info("CoapServer on '{}' port '{}' exposes '{}' at coap://{}:{}/{}", bindHost, bindPort,
                thing.getId(), bindHost, bindPort, thing.getId());

        if (server == null) {
            return failedFuture(new ProtocolServerException("Unable to expose thing before CoapServer has been started"));
        }

        things.put(thing.getId(), thing);

        CoapResource thingResource = new ThingResource(thing);
        resources.put(thing.getId(), thingResource);

        Resource root = server.getRoot();
        if (root == null) {
            return failedFuture(new Exception("Unable to expose thing before CoapServer has been started"));
        }
        root.add(thingResource);

        exposeProperties(thing, thingResource, addresses, ContentManager.getOfferedMediaTypes());
        exposeActions(thing, thingResource, addresses, ContentManager.getOfferedMediaTypes());
        exposeEvents(thing, thingResource, addresses, ContentManager.getOfferedMediaTypes());

        return completedFuture(null);
    }

    @Override
    public CompletableFuture<Void> destroy(ExposedThing thing) {
        log.info("CoapServer on '{}' port '{}' stop exposing '{}' at coap://{}:{}/{}", bindHost, bindPort,
                thing.getId(), bindHost, bindPort, thing.getId());

        if (server == null) {
            return completedFuture(null);
        }

        things.remove(thing.getId());

        CoapResource resource = resources.remove(thing.getId());
        if (resource != null) {
            server.getRoot().delete(resource);
        }

        return completedFuture(null);
    }

    @Override
    public URI getDirectoryUrl() {
        try {
            return new URI(addresses.get(0));
        }
        catch (URISyntaxException e) {
            log.warn("Unable to create directory url", e);
            return null;
        }
    }

    @Override
    public URI getThingUrl(String id) {
        try {
            return new URI(addresses.get(0)).resolve("/" + id);
        }
        catch (URISyntaxException e) {
            log.warn("Unable to thing url", e);
            return null;
        }
    }

    private void exposeProperties(ExposedThing thing,
                                  CoapResource thingResource,
                                  List<String> addresses,
                                  Set<String> contentTypes) {
        Map<String, ExposedThingProperty<Object>> properties = thing.getProperties();
        if (!properties.isEmpty()) {
            // make reporting of all properties optional?
            if (true) {
                CoapResource allResource = new CoapResource("all");
                thingResource.add(allResource);

                for (String address : addresses) {
                    for (String contentType : contentTypes) {
                        String href = address + "/" + thing.getId() + "/all/properties";
                        Form form = new Form.Builder()
                                .setHref(href)
                                .setContentType(contentType)
                                .setOp(Operation.READ_ALL_PROPERTIES, Operation.READ_MULTIPLE_PROPERTIES/*, Operation.writeallproperties, Operation.writemultipleproperties*/)
                                .build();

                        thing.addForm(form);
                        log.debug("Assign '{}' for reading all properties", href);
                    }
                }

                allResource.add(new AllPropertiesResource(thing));
            }

            CoapResource propertiesResource = new CoapResource("properties");
            thingResource.add(propertiesResource);

            properties.forEach((name, property) -> {
                for (String address : addresses) {
                    for (String contentType : contentTypes) {
                        String href = address + "/" + thing.getId() + "/properties/" + name;
                        Form.Builder form = new Form.Builder()
                                .setHref(href)
                                .setContentType(contentType);
                        if (property.isReadOnly()) {
                            form.setOp(Operation.READ_PROPERTY);
                        }
                        else if (property.isWriteOnly()) {
                            form.setOp(Operation.WRITE_PROPERTY);
                        }
                        else {
                            form.setOp(Operation.READ_PROPERTY, Operation.WRITE_PROPERTY);
                        }

                        property.addForm(form.build());
                        log.debug("Assign '{}' to Property '{}'", href, name);

                        // if property is observable add an additional form with a observable href
                        if (property.isObservable()) {
                            String observableHref = href + "/observable";
                            Form observableForm = new Form.Builder()
                                    .setHref(observableHref)
                                    .setContentType(contentType)
                                    .setOp(Operation.OBSERVE_PROPERTY)
                                    .setSubprotocol("longpoll")
                                    .build();

                            property.addForm(observableForm);
                            log.debug("Assign '{}' to observe Property '{}'", observableHref, name);
                        }
                    }
                }

                PropertyResource propertyResource = new PropertyResource(name, property);
                propertiesResource.add(propertyResource);

                if (property.isObservable()) {
                    propertyResource.add(new ObservePropertyResource(name, property));
                }
            });
        }
    }

    private void exposeActions(ExposedThing thing,
                               CoapResource thingResource,
                               List<String> addresses,
                               Set<String> contentTypes) {
        Map<String, ExposedThingAction<Object, Object>> actions = thing.getActions();
        if (!actions.isEmpty()) {
            CoapResource actionsResource = new CoapResource("actions");
            thingResource.add(actionsResource);

            actions.forEach((name, action) -> {
                for (String address : addresses) {
                    for (String contentType : contentTypes) {
                        String href = address + "/" + thing.getId() + "/actions/" + name;
                        Form form = new Form.Builder()
                                .setHref(href)
                                .setOp(Operation.INVOKE_ACTION)
                                .setContentType(contentType)
                                .build();

                        action.addForm(form);
                        log.debug("Assign '{}' to Action '{}'", href, name);
                    }
                }

                actionsResource.add(new ActionResource(name, action));
            });
        }
    }

    private void exposeEvents(ExposedThing thing,
                              CoapResource thingResource,
                              List<String> addresses,
                              Set<String> contentTypes) {
        Map<String, ExposedThingEvent<Object>> events = thing.getEvents();
        if (!events.isEmpty()) {
            CoapResource eventsResource = new CoapResource("events");
            thingResource.add(eventsResource);

            events.forEach((name, event) -> {
                for (String address : addresses) {
                    for (String contentType : contentTypes) {
                        String href = address + "/" + thing.getId() + "/events/" + name;
                        Form form = new Form.Builder()
                                .setHref(href)
                                .setOp(Operation.SUBSCRIBE_EVENT)
                                .setContentType(contentType)
                                .build();

                        event.addForm(form);
                        log.debug("Assign '{}' to Event '{}'", href, name);
                    }
                }

                eventsResource.add(new EventResource(name, event));
            });
        }
    }

    /**
     * This method blocks until the port specified in <code>port</code> is available (again). The
     * maximum blocking time is 10 seconds. If the port does not become available within 10 seconds,
     * a {@link TimeoutException} is thrown.
     *
     * @param port
     * @throws TimeoutException
     */
    public static void waitForPort(int port) throws TimeoutException {
        waitForPort(port, Duration.ofSeconds(10));
    }

    /**
     * This method blocks until the port specified in <code>port</code> is available (again). The
     * maximum blocking time is specified with <code>duration</code>. If the port does not become
     * available within the specified duration, a {@link TimeoutException} is thrown.
     *
     * @param port
     * @param duration
     * @throws TimeoutException
     */
    public static void waitForPort(int port, Duration duration) throws TimeoutException {
        CompletableFuture<Boolean> result = new CompletableFuture<>();

        Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                try (ServerSocket socket = new ServerSocket(port)) {
                    result.complete(true);
                }
                catch (IOException e) {
                    // ignore
                }
            }
        }, 0, 100);

        try {
            result.get(duration.toMillis(), TimeUnit.MILLISECONDS);
        }
        catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        catch (ExecutionException e) {
            // ignore
        }
    }

    public int getBindPort() {
        return bindPort;
    }

    public Map<String, ExposedThing> getThings() {
        return things;
    }
}
