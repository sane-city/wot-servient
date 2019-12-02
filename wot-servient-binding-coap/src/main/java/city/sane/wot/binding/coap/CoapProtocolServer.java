package city.sane.wot.binding.coap;

import city.sane.wot.Servient;
import city.sane.wot.binding.ProtocolServer;
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

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

/**
 * Allows exposing Things via CoAP.
 */
public class CoapProtocolServer implements ProtocolServer {
    static final Logger log = LoggerFactory.getLogger(CoapProtocolServer.class);

    static {
        // Californium uses java.util.logging. We need to redirect all log messages to logback
        SLF4JBridgeHandler.removeHandlersForRootLogger();
        SLF4JBridgeHandler.install();
    }

    private final int bindPort;
    private final List<String> addresses;

    private final Map<String, ExposedThing> things = new HashMap<>();
    private final Map<String, CoapResource> resources = new HashMap<String, CoapResource>();
    private CoapServer server;

    public CoapProtocolServer(Config config) {
        bindPort = config.getInt("wot.servient.coap.bind-port");
        if (!config.getStringList("wot.servient.coap.addresses").isEmpty()) {
            addresses = config.getStringList("wot.servient.coap.addresses");
        }
        else {
            addresses = Servient.getAddresses().stream().map(a -> "coap://" + a + ":" + bindPort).collect(Collectors.toList());
        }
    }

    @Override
    public String toString() {
        return "CoapServer";
    }

    @Override
    public CompletableFuture<Void> start() {
        log.info("Starting on port '{}'", bindPort);

        return CompletableFuture.runAsync(() -> {
            server = new CoapServer(this);
            server.start();
        });
    }

    @Override
    public CompletableFuture<Void> stop() {
        log.info("Stopping on port '{}'", bindPort);

        return CompletableFuture.runAsync(() -> {
            server.stop();
            server.destroy();

            // TODO: Wait some time after the server has shut down. Apparently the CoAP server reports too early that it was terminated, even though the port is
            //  still in use. This sometimes led to errors during the tests because other CoAP servers were not able to be started because the port was already
            //  in use. This error only occurred in the GitLab CI (in Docker). Instead of waiting, the error should be reported to the maintainer of the CoAP
            //  server and fixed. Because the isolation of the error is so complex, this workaround was chosen.
            try {
                Thread.sleep(1 * 1000L);
            }
            catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }

            log.debug("Server stopped");
        });
    }

    @Override
    public CompletableFuture<Void> expose(ExposedThing thing) {
        log.info("CoapServer on '{}' exposes '{}' at coap://0.0.0.0:{}/{}", bindPort,
                thing.getTitle(), bindPort, thing.getId());
        things.put(thing.getId(), thing);

        CoapResource thingResource = new ThingResource(server, thing);
        resources.put(thing.getId(), thingResource);

        Resource root = server.getRoot();
        if (root == null) {
            return CompletableFuture.failedFuture(new Exception("Unable to expose thing before CoapServer has been started"));
        }
        root.add(thingResource);

        for (String address : addresses) {
            for (String contentType : ContentManager.getOfferedMediaTypes()) {
                //
                // properties
                //

                Map<String, ExposedThingProperty> properties = thing.getProperties();
                if (!properties.isEmpty()) {
                    // make reporting of all properties optional?
                    if (true) {
                        CoapResource allResource = new CoapResource("all");
                        thingResource.add(allResource);

                        String href = address + "/" + thing.getId() + "/all/properties";
                        Form form = new Form.Builder()
                                .setHref(href)
                                .setContentType(contentType)
                                .setOp(Arrays.asList(Operation.readallproperties, Operation.readmultipleproperties/*, Operation.writeallproperties, Operation.writemultipleproperties*/))
                                .build();

                        thing.addForm(form);
                        log.info("Assign '{}' for reading all properties", href);

                        allResource.add(new AllPropertiesResource(server, thing));
                    }

                    CoapResource propertiesResource = new CoapResource("properties");
                    thingResource.add(propertiesResource);

                    properties.forEach((name, property) -> {
                        String href = address + "/" + thing.getId() + "/properties/" + name;
                        Form.Builder form = new Form.Builder()
                                .setHref(href)
                                .setContentType(contentType);
                        if (property.isReadOnly()) {
                            form.setOp(Operation.readproperty);
                        }
                        else if (property.isWriteOnly()) {
                            form.setOp(Operation.writeproperty);
                        }
                        else {
                            form.setOp(Arrays.asList(Operation.readproperty, Operation.writeproperty));
                        }

                        property.addForm(form.build());
                        log.info("Assign '{}' to Property '{}'", href, name);

                        PropertyResource propertyResource = new PropertyResource(server, name, property);
                        propertiesResource.add(propertyResource);

                        // if property is observable add an additional form with a observable href
                        if (property.isObservable()) {
                            String observableHref = href + "/observable";
                            Form observableForm = new Form.Builder()
                                    .setHref(observableHref)
                                    .setContentType(contentType)
                                    .setOp(Operation.observeproperty)
                                    .setSubprotocol("longpoll")
                                    .build();

                            property.addForm(observableForm);
                            log.info("Assign '{}' to observable Property '{}'", observableHref, name);

                            propertyResource.add(new ObservePropertyResource(server, name, property));
                        }
                    });
                }

                //
                // actions
                //
                Map<String, ExposedThingAction> actions = thing.getActions();
                if (!actions.isEmpty()) {
                    CoapResource actionsResource = new CoapResource("actions");
                    thingResource.add(actionsResource);

                    actions.forEach((name, action) -> {
                        String href = address + "/" + thing.getId() + "/actions/" + name;
                        Form form = new Form.Builder()
                                .setHref(href)
                                .setOp(Operation.invokeaction)
                                .setContentType(contentType)
                                .build();

                        action.addForm(form);
                        log.info("Assign '{}' to Action '{}'", href, name);

                        actionsResource.add(new ActionResource(server, name, action));
                    });
                }

                //
                // events
                //
                Map<String, ExposedThingEvent> events = thing.getEvents();
                if (!events.isEmpty()) {
                    CoapResource eventsResource = new CoapResource("events");
                    thingResource.add(eventsResource);

                    events.forEach((name, event) -> {
                        String href = address + "/" + thing.getId() + "/events/" + name;
                        Form form = new Form.Builder()
                                .setHref(href)
                                .setOp(Operation.subscribeevent)
                                .setContentType(contentType)
                                .build();

                        event.addForm(form);
                        log.info("Assign '{}' to Event '{}'", href, name);

                        eventsResource.add(new EventResource(server, name, event));
                    });
                }
            }
        }

        return CompletableFuture.completedFuture(null);
    }

    @Override
    public CompletableFuture<Void> destroy(ExposedThing thing) {
        log.info("CoapServer on '{}' stop exposing '{}' at coap://0.0.0.0:{}/{}", bindPort,
                thing.getTitle(), bindPort, thing.getId());
        things.remove(thing.getId());

        CoapResource resource = resources.remove(thing.getId());
        if (resource != null) {
            server.getRoot().delete(resource);
        }

        return CompletableFuture.completedFuture(null);
    }

    @Override
    public URI getDirectoryUrl() {
        try {
            return new URI(addresses.get(0));
        }
        catch (URISyntaxException e) {
            log.warn("Unable to create directory url: {}", e);
            return null;
        }
    }

    @Override
    public URI getThingUrl(String id) {
        try {
            return new URI(addresses.get(0) + "/" + id);
        }
        catch (URISyntaxException e) {
            log.warn("Unable to thing url: {}", e);
            return null;
        }
    }

    public int getBindPort() {
        return bindPort;
    }

    public Map<String, ExposedThing> getThings() {
        return things;
    }
}
