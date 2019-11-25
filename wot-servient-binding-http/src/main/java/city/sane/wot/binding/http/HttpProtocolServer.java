package city.sane.wot.binding.http;

import city.sane.wot.Servient;
import city.sane.wot.binding.ProtocolServer;
import city.sane.wot.binding.http.route.*;
import city.sane.wot.content.ContentManager;
import city.sane.wot.thing.ExposedThing;
import city.sane.wot.thing.ThingInteraction;
import city.sane.wot.thing.action.ExposedThingAction;
import city.sane.wot.thing.event.ExposedThingEvent;
import city.sane.wot.thing.form.Form;
import city.sane.wot.thing.form.Operation;
import city.sane.wot.thing.property.ExposedThingProperty;
import com.typesafe.config.Config;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import spark.Service;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

/**
 * Allows exposing Things via HTTP.
 */
public class HttpProtocolServer implements ProtocolServer {
    static final Logger log = LoggerFactory.getLogger(HttpProtocolServer.class);

    private final String bindHost;
    private final int bindPort;
    private final List<String> addresses;

    private final Service server;
    private final Map<String, ExposedThing> things = new HashMap<>();

    public HttpProtocolServer(Config config) {
        bindHost = config.getString("wot.servient.http.bind-host");
        bindPort = config.getInt("wot.servient.http.bind-port");
        if (!config.getStringList("wot.servient.http.addresses").isEmpty()) {
            addresses = config.getStringList("wot.servient.http.addresses");
        }
        else {
            addresses = Servient.getAddresses().stream().map(a -> "ws://" + a + ":" + bindPort + "/things").collect(Collectors.toList());
        }

        server = Service.ignite().ipAddress(bindHost).port(bindPort);
    }

    @Override
    public String toString() {
        return "HttpServer";
    }

    @Override
    public CompletableFuture<Void> start() {
        log.info("Starting on '{}' port '{}'", bindHost, bindPort);

        return CompletableFuture.runAsync(() -> {
            server.defaultResponseTransformer(new ContentResponseTransformer());
            server.init();
            server.awaitInitialization();

            server.path("/things", () -> {
                server.get("", new ThingsRoute(things));
                server.path("/:id", () -> {
                    server.path("/properties/:name", () -> {
                        server.get("/observable", new ObservePropertyRoute(things));
                        server.get("", new ReadPropertyRoute(things));
                        server.put("", new WritePropertyRoute(things));
                    });
                    server.post("/actions/:name", new InvokeActionRoute(things));
                    server.get("/events/:name", new SubscribeEventRoute(things));
                    server.path("/all", () -> {
                        server.get("/properties", new ReadAllPropertiesRoute(things));
                    });
                    server.get("", new ThingRoute(things));
                });
            });
        });
    }

    @Override
    public CompletableFuture<Void> stop() {
        log.info("Stopping on port '{}'", bindPort);

        return CompletableFuture.runAsync(() -> {
            server.stop();
            server.awaitStop();
        });
    }

    @Override
    public CompletableFuture<Void> expose(ExposedThing thing) {
        log.info("HttpServer on '{}' exposes '{}' at http://{}:{}/things/{}", bindPort, thing.getTitle(),
                bindHost, bindPort, thing.getId());
        things.put(thing.getId(), thing);

        for (String address : addresses) {
            for (String contentType : ContentManager.getOfferedMediaTypes()) {
                //
                // properties
                //

                // make reporting of all properties optional?
                if (true) {
                    String href = address + "/" + thing.getId() + "/all/properties";
                    Form form = new Form.Builder()
                            .setHref(href)
                            .setContentType(contentType)
                            .setOp(Arrays.asList(Operation.readallproperties, Operation.readmultipleproperties/*, Operation.writeallproperties, Operation.writemultipleproperties*/))
                            .build();

                    thing.addForm(form);
                    log.info("Assign '{}' for reading all properties", href);
                }

                Map<String, ExposedThingProperty> properties = thing.getProperties();
                properties.forEach((name, property) -> {
                    String href = getHrefWithVariablePattern(address, thing, "properties", name, property);
                    Form.Builder form = new Form.Builder();
                    form.setHref(href);
                    form.setContentType(contentType);
                    if (property.isReadOnly()) {
                        form.setOp(Operation.readproperty);
                        form.setOptional("htv:methodName", "GET");
                    }
                    else if (property.isWriteOnly()) {
                        form.setOp(Operation.writeproperty);
                        form.setOptional("htv:methodName", "PUT");
                    }
                    else {
                        form.setOp(Arrays.asList(Operation.readproperty, Operation.writeproperty));
                    }

                    property.addForm(form.build());
                    log.info("Assign '{}' to Property '{}'", href, name);

                    // if property is observable add an additional form with a observable href
                    if (property.isObservable()) {
                        String observableHref = href + "/observable";
                        Form.Builder observableForm = new Form.Builder();
                        observableForm.setHref(observableHref);
                        observableForm.setContentType(contentType);
                        observableForm.setOp(Operation.observeproperty);
                        observableForm.setSubprotocol("longpoll");

                        property.addForm(observableForm.build());
                        log.info("Assign '{}' to observable Property '{}'", observableHref, name);
                    }
                });

                //
                // actions
                //
                Map<String, ExposedThingAction> actions = thing.getActions();
                actions.forEach((name, action) -> {
                    String href = getHrefWithVariablePattern(address, thing, "actions", name, action);
                    Form.Builder form = new Form.Builder();
                    form.setHref(href);
                    form.setContentType(contentType);
                    form.setOp(Operation.invokeaction);
                    form.setOptional("htv:methodName", "POST");

                    action.addForm(form.build());
                    log.info("Assign '{}' to Action '{}'", href, name);
                });

                //
                // events
                //
                Map<String, ExposedThingEvent> events = thing.getEvents();
                events.forEach((name, event) -> {
                    String href = getHrefWithVariablePattern(address, thing, "events", name, event);
                    Form.Builder form = new Form.Builder();
                    form.setHref(href);
                    form.setContentType(contentType);
                    form.setSubprotocol("longpoll");
                    form.setOp(Operation.subscribeevent);

                    event.addForm(form.build());
                    log.info("Assign '{}' to Event '{}'", href, name);
                });
            }
        }

        return CompletableFuture.completedFuture(null);
    }

    @Override
    public CompletableFuture<Void> destroy(ExposedThing thing) {
        log.info("HttpServer on '{}' stop exposing '{}' at http://{}:{}/{}", bindPort, thing.getTitle(),
                bindHost, bindPort, thing.getId());
        things.remove(thing.getId());

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

    private String getHrefWithVariablePattern(String address, ExposedThing thing, String type, String interactionName, ThingInteraction interaction) {
        String variables = "";
        Set<String> uriVariables = interaction.getUriVariables().keySet();
        if (!uriVariables.isEmpty()) {
            variables = "{?" + String.join(",", uriVariables) + "}";
        }

        return address + "/" + thing.getId() + "/" + type + "/" + interactionName + variables;
    }

    public Service getHTTPServer () {
        return this.server;
    }
}
