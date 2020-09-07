package city.sane.wot.binding.http;

import city.sane.wot.Servient;
import city.sane.wot.binding.ProtocolServer;
import city.sane.wot.binding.ProtocolServerException;
import city.sane.wot.binding.http.route.InvokeActionRoute;
import city.sane.wot.binding.http.route.ObservePropertyRoute;
import city.sane.wot.binding.http.route.ReadAllPropertiesRoute;
import city.sane.wot.binding.http.route.ReadPropertyRoute;
import city.sane.wot.binding.http.route.SubscribeEventRoute;
import city.sane.wot.binding.http.route.ThingRoute;
import city.sane.wot.binding.http.route.ThingsRoute;
import city.sane.wot.binding.http.route.WritePropertyRoute;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

import static java.util.concurrent.CompletableFuture.completedFuture;
import static java.util.concurrent.CompletableFuture.failedFuture;
import static java.util.concurrent.CompletableFuture.runAsync;

/**
 * Allows exposing Things via HTTP.
 */
public class HttpProtocolServer implements ProtocolServer {
    private static final Logger log = LoggerFactory.getLogger(HttpProtocolServer.class);
    private static final String HTTP_METHOD_NAME = "htv:methodName";
    private static final String SECURITY_SCHEME = "scheme";
    private final String bindHost;
    private final int bindPort;
    private final List<String> addresses;
    private final Service server;
    private final Map<String, ExposedThing> things;
    private final Map<String, Object> security;
    private final String securityScheme;
    private boolean started = false;
    private int actualPort;
    private List<String> actualAddresses;

    public HttpProtocolServer(Config config) throws ProtocolServerException {
        bindHost = config.getString("wot.servient.http.bind-host");
        bindPort = config.getInt("wot.servient.http.bind-port");
        if (!config.getStringList("wot.servient.http.addresses").isEmpty()) {
            addresses = config.getStringList("wot.servient.http.addresses");
        }
        else {
            addresses = Servient.getAddresses().stream().map(a -> "http://" + a + ":" + bindPort).collect(Collectors.toList());
        }
        things = new HashMap<>();
        security = config.getObject("wot.servient.http.security").unwrapped();

        // Auth
        if (security != null && security.get(SECURITY_SCHEME) != null) {
            // storing HTTP header compatible string
            switch ((String) security.get(SECURITY_SCHEME)) {
                case "basic":
                    this.securityScheme = "Basic";
                    break;
                case "bearer":
                    this.securityScheme = "Bearer";
                    break;
                default:
                    throw new ProtocolServerException("HttpServer does not support security scheme '" + security.get(SECURITY_SCHEME) + "'");
            }
        }
        else {
            this.securityScheme = null;
        }

        server = Service.ignite().ipAddress(bindHost).port(bindPort);
    }

    @SuppressWarnings({ "java:S107" })
    HttpProtocolServer(String bindHost,
                       int bindPort,
                       List<String> addresses,
                       Service server,
                       Map<String, ExposedThing> things,
                       Map<String, Object> security,
                       String securityScheme,
                       boolean started,
                       int actualPort,
                       List<String> actualAddresses) {
        this.bindHost = bindHost;
        this.bindPort = bindPort;
        this.addresses = addresses;
        this.server = server;
        this.things = things;
        this.security = security;
        this.securityScheme = securityScheme;
        this.started = started;
        this.actualPort = actualPort;
        this.actualAddresses = actualAddresses;
    }

    @Override
    public String toString() {
        return "HttpServer";
    }

    @Override
    public CompletableFuture<Void> start(Servient servient) {
        log.info("Starting on '{}' port '{}'", bindHost, bindPort);

        return runAsync(() -> {
            server.defaultResponseTransformer(new ContentResponseTransformer());
            server.init();
            server.awaitInitialization();

            server.get("/", new ThingsRoute(things));
            server.path("/:id", () -> {
                server.path("/properties/:name", () -> {
                    server.get("/observable", new ObservePropertyRoute(servient, securityScheme, things));
                    server.get("", new ReadPropertyRoute(servient, securityScheme, things));
                    server.put("", new WritePropertyRoute(servient, securityScheme, things));
                });
                server.post("/actions/:name", new InvokeActionRoute(servient, securityScheme, things));
                server.get("/events/:name", new SubscribeEventRoute(servient, securityScheme, things));
                server.path("/all", () -> server.get("/properties", new ReadAllPropertiesRoute(servient, securityScheme, things)));
                server.get("", new ThingRoute(servient, securityScheme, things));
            });

            actualPort = server.port();
            actualAddresses = addresses.stream()
                    .map(a -> a.replace(":" + bindPort, ":" + actualPort))
                    .collect(Collectors.toList());

            started = true;
        });
    }

    @Override
    public CompletableFuture<Void> stop() {
        log.info("Stopping on '{}' port '{}'", bindHost, actualPort);

        return runAsync(() -> {
            started = false;
            server.stop();
            server.awaitStop();
        });
    }

    @Override
    public CompletableFuture<Void> expose(ExposedThing thing) {
        if (!started) {
            return failedFuture(new ProtocolServerException("Unable to expose thing before HttpServer has been started"));
        }

        log.info("HttpServer on '{}' port '{}' exposes '{}' at http://{}:{}/{}", bindHost, actualPort, thing.getId(),
                bindHost, actualPort, thing.getId());

        things.put(thing.getId(), thing);

        for (String address : actualAddresses) {
            for (String contentType : ContentManager.getOfferedMediaTypes()) {
                // make reporting of all properties optional?
                if (true) {
                    String href = address + "/" + thing.getId() + "/all/properties";
                    Form form = new Form.Builder()
                            .setHref(href)
                            .setContentType(contentType)
                            .setOp(Operation.READ_ALL_PROPERTIES, Operation.READ_MULTIPLE_PROPERTIES/*, Operation.writeallproperties, Operation.writemultipleproperties*/)
                            .build();

                    thing.addForm(form);
                    log.debug("Assign '{}' for reading all properties", href);
                }

                exposeProperties(thing, address, contentType);
                exposeActions(thing, address, contentType);
                exposeEvents(thing, address, contentType);
            }
        }

        return completedFuture(null);
    }

    @Override
    public CompletableFuture<Void> destroy(ExposedThing thing) {
        // if the server is not running, nothing needs to be done
        if (!started) {
            return completedFuture(null);
        }

        log.info("HttpServer on '{}' port '{}' stop exposing '{}' at http://{}:{}/{}", bindHost, actualPort, thing.getId(),
                bindHost, actualPort, thing.getId());
        things.remove(thing.getId());

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

    private void exposeProperties(ExposedThing thing, String address, String contentType) {
        Map<String, ExposedThingProperty<Object>> properties = thing.getProperties();
        properties.forEach((name, property) -> {
            String href = getHrefWithVariablePattern(address, thing, "properties", name, property);
            Form.Builder form = new Form.Builder();
            form.setHref(href);
            form.setContentType(contentType);
            if (property.isReadOnly()) {
                form.setOp(Operation.READ_PROPERTY);
                form.setOptional(HTTP_METHOD_NAME, "GET");
            }
            else if (property.isWriteOnly()) {
                form.setOp(Operation.WRITE_PROPERTY);
                form.setOptional(HTTP_METHOD_NAME, "PUT");
            }
            else {
                form.setOp(Operation.READ_PROPERTY, Operation.WRITE_PROPERTY);
            }

            property.addForm(form.build());
            log.debug("Assign '{}' to Property '{}'", href, name);

            // if property is observable add an additional form with a observable href
            if (property.isObservable()) {
                String observableHref = href + "/observable";
                Form.Builder observableForm = new Form.Builder();
                observableForm.setHref(observableHref);
                observableForm.setContentType(contentType);
                observableForm.setOp(Operation.OBSERVE_PROPERTY);
                observableForm.setSubprotocol("longpoll");

                property.addForm(observableForm.build());
                log.debug("Assign '{}' to observe Property '{}'", observableHref, name);
            }
        });
    }

    private void exposeActions(ExposedThing thing, String address, String contentType) {
        Map<String, ExposedThingAction<Object, Object>> actions = thing.getActions();
        actions.forEach((name, action) -> {
            String href = getHrefWithVariablePattern(address, thing, "actions", name, action);
            Form.Builder form = new Form.Builder();
            form.setHref(href);
            form.setContentType(contentType);
            form.setOp(Operation.INVOKE_ACTION);
            form.setOptional(HTTP_METHOD_NAME, "POST");

            action.addForm(form.build());
            log.debug("Assign '{}' to Action '{}'", href, name);
        });
    }

    private void exposeEvents(ExposedThing thing, String address, String contentType) {
        Map<String, ExposedThingEvent<Object>> events = thing.getEvents();
        events.forEach((name, event) -> {
            String href = getHrefWithVariablePattern(address, thing, "events", name, event);
            Form.Builder form = new Form.Builder();
            form.setHref(href);
            form.setContentType(contentType);
            form.setSubprotocol("longpoll");
            form.setOp(Operation.SUBSCRIBE_EVENT);

            event.addForm(form.build());
            log.debug("Assign '{}' to Event '{}'", href, name);
        });
    }

    private String getHrefWithVariablePattern(String address,
                                              ExposedThing thing,
                                              String type,
                                              String interactionName,
                                              ThingInteraction interaction) {
        String variables = "";
        Set<String> uriVariables = interaction.getUriVariables().keySet();
        if (!uriVariables.isEmpty()) {
            variables = "{?" + String.join(",", uriVariables) + "}";
        }

        return address + "/" + thing.getId() + "/" + type + "/" + interactionName + variables;
    }

    public int getPort() {
        return actualPort;
    }
}
