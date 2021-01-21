/*
 * Copyright (c) 2021.
 *
 * This file is part of SANE Web of Things Servient.
 *
 * SANE Web of Things Servient is free software: you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * SANE Web of Things Servient is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with SANE Web of Things Servient.  If not, see
 * <http://www.gnu.org/licenses/>.
 */
package city.sane.wot.binding.coap;

import city.sane.wot.Servient;
import city.sane.wot.binding.ProtocolServer;
import city.sane.wot.binding.ProtocolServerException;
import city.sane.wot.binding.coap.resource.ActionResource;
import city.sane.wot.binding.coap.resource.AllPropertiesResource;
import city.sane.wot.binding.coap.resource.EventResource;
import city.sane.wot.binding.coap.resource.ObservePropertyResource;
import city.sane.wot.binding.coap.resource.PropertyResource;
import city.sane.wot.binding.coap.resource.ThingResource;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import static java.util.concurrent.CompletableFuture.completedFuture;
import static java.util.concurrent.CompletableFuture.failedFuture;
import static java.util.concurrent.CompletableFuture.runAsync;

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
    private int actualPort;
    private List<String> actualAddresses;

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

    @SuppressWarnings({ "java:S107" })
    CoapProtocolServer(String bindHost,
                       int bindPort,
                       List<String> addresses,
                       Map<String, ExposedThing> things,
                       Map<String, CoapResource> resources,
                       Supplier<WotCoapServer> serverSupplier,
                       WotCoapServer server,
                       int actualPort,
                       List<String> actualAddresses) {
        this.bindHost = bindHost;
        this.bindPort = bindPort;
        this.addresses = addresses;
        this.things = things;
        this.resources = resources;
        this.serverSupplier = serverSupplier;
        this.server = server;
        this.actualPort = actualPort;
        this.actualAddresses = actualAddresses;
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
            actualPort = server.getPort();
            actualAddresses = addresses.stream()
                    .map(a -> a.replace(":" + bindPort, ":" + actualPort))
                    .collect(Collectors.toList());
        });
    }

    @Override
    public CompletableFuture<Void> stop() {
        log.info("Stopping on '{}' port '{}'", bindHost, actualPort);

        if (server == null) {
            return completedFuture(null);
        }

        return runAsync(() -> {
            server.stop();
            server.destroy();

            log.debug("Server stopped");
        });
    }

    @Override
    public CompletableFuture<Void> expose(ExposedThing thing) {
        if (server == null) {
            return failedFuture(new ProtocolServerException("Unable to expose thing before CoapServer has been started"));
        }

        log.info("CoapServer on '{}' port '{}' exposes '{}' at coap://{}:{}/{}", bindHost, actualPort,
                thing.getId(), bindHost, actualPort, thing.getId());

        things.put(thing.getId(), thing);

        CoapResource thingResource = new ThingResource(thing);
        resources.put(thing.getId(), thingResource);

        Resource root = server.getRoot();
        if (root == null) {
            return failedFuture(new Exception("Unable to expose thing before CoapServer has been started"));
        }
        root.add(thingResource);

        exposeProperties(thing, thingResource, actualAddresses, ContentManager.getOfferedMediaTypes());
        exposeActions(thing, thingResource, actualAddresses, ContentManager.getOfferedMediaTypes());
        exposeEvents(thing, thingResource, actualAddresses, ContentManager.getOfferedMediaTypes());

        return completedFuture(null);
    }

    @Override
    public CompletableFuture<Void> destroy(ExposedThing thing) {
        // if the server is not running, nothing needs to be done
        if (server == null) {
            return completedFuture(null);
        }

        log.info("CoapServer on '{}' port '{}' stop exposing '{}' at coap://{}:{}/{}", bindHost, actualPort,
                thing.getId(), bindHost, actualPort, thing.getId());

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
            return new URI(actualAddresses.get(0));
        }
        catch (URISyntaxException e) {
            log.warn("Unable to create directory url", e);
            return null;
        }
    }

    @Override
    public URI getThingUrl(String id) {
        try {
            return new URI(actualAddresses.get(0)).resolve("/" + id);
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
                addAllPropertiesForm(thing, thingResource, addresses, contentTypes);
            }

            CoapResource propertiesResource = new CoapResource("properties");
            thingResource.add(propertiesResource);

            properties.forEach((name, property) -> addPropertyForm(thing, addresses, contentTypes, propertiesResource, name, property));
        }
    }

    private void addPropertyForm(ExposedThing thing,
                                 List<String> addresses,
                                 Set<String> contentTypes,
                                 CoapResource propertiesResource,
                                 String name,
                                 ExposedThingProperty<Object> property) {
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
    }

    private void addAllPropertiesForm(ExposedThing thing,
                                      CoapResource thingResource,
                                      List<String> addresses,
                                      Set<String> contentTypes) {
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

    public int getBindPort() {
        return bindPort;
    }

    public Map<String, ExposedThing> getThings() {
        return things;
    }
}
