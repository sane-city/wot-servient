package city.sane.wot.thing;

import city.sane.Pair;
import city.sane.wot.Servient;
import city.sane.wot.binding.ProtocolClient;
import city.sane.wot.binding.ProtocolClientException;
import city.sane.wot.content.Content;
import city.sane.wot.content.ContentCodecException;
import city.sane.wot.content.ContentManager;
import city.sane.wot.thing.action.ConsumedThingAction;
import city.sane.wot.thing.action.ThingAction;
import city.sane.wot.thing.event.ConsumedThingEvent;
import city.sane.wot.thing.event.ThingEvent;
import city.sane.wot.thing.form.Form;
import city.sane.wot.thing.form.Operation;
import city.sane.wot.thing.property.ConsumedThingProperty;
import city.sane.wot.thing.property.ThingProperty;
import city.sane.wot.thing.schema.ObjectSchema;
import city.sane.wot.thing.security.SecurityScheme;
import com.damnhandy.uri.template.UriTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Represents an object that extends a Thing with methods for client interactions (send request for reading and
 * writing Properties), invoke Actions, subscribe and unsubscribe for Property changes and Events.
 * https://w3c.github.io/wot-scripting-api/#the-consumedthing-interface
 */
public class ConsumedThing extends Thing<ConsumedThingProperty, ConsumedThingAction, ConsumedThingEvent> {
    static final Logger log = LoggerFactory.getLogger(ConsumedThing.class);

    public static final String DEFAULT_OBJECT_TYPE = "Thing";
    public static final Context DEFAULT_OBJECT_CONTEXT = new Context("https://www.w3.org/2019/wot/td/v1");

    private final Servient servient;
    private final Map<String, ProtocolClient> clients = new HashMap<>();

    public ConsumedThing(Servient servient, Thing thing) {
        this.servient = servient;
        if (thing != null) {
            objectType = thing.getObjectType();
            if (objectType == null) {
                objectType = DEFAULT_OBJECT_TYPE;
            }
            objectContext = thing.getObjectContext();
            if (objectContext == null) {
                objectContext = DEFAULT_OBJECT_CONTEXT;
            }
            id = thing.getId();
            title = thing.getTitle();
            titles = thing.getTitles();
            description = thing.getDescription();
            descriptions = thing.getDescriptions();
            forms = thing.getForms();
            security = thing.getSecurity();
            securityDefinitions = thing.getSecurityDefinitions();
            base = thing.getBase();

            Map<String, ThingProperty> properties = thing.getProperties();
            properties.forEach((name, property) -> this.properties.put(name, new ConsumedThingProperty(name, property, this)));

            Map<String, ThingAction> actions = thing.getActions();
            actions.forEach((name, action) -> this.actions.put(name, new ConsumedThingAction(name, action, this)));

            Map<String, ThingEvent> events = thing.getEvents();
            events.forEach((name, event) -> this.events.put(name, new ConsumedThingEvent(name, event, this)));
        }
    }

    @Override
    public int hashCode() {
        return super.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        return super.equals(obj);
    }

    /**
     * Searches and returns a ProtocolClient in given <code>forms</code> that matches the given <code>op</code>.
     * Throws an exception when no client can be found.
     *
     * @param forms
     * @param op
     *
     * @return
     * @throws ConsumedThingException
     */
    public Pair<ProtocolClient, Form> getClientFor(List<Form> forms, Operation op) throws ConsumedThingException {
        if (forms.isEmpty()) {
            throw new NoFormForInteractionConsumedThingException(getTitle(), op);
        }

        Set<String> schemes = forms.stream().map(Form::getHrefScheme)
                .collect(Collectors.toCollection(LinkedHashSet::new));
        // TODO: sort schemes by priority?

        String scheme = null;
        ProtocolClient client = null;
        for (String s : schemes) {
            ProtocolClient c = clients.get(s);
            if (c != null) {
                scheme = s;
                client = c;
                break;
            }
        }

        if (client != null) {
            // from cache
            log.debug("'{}' chose cached client for '{}'", getTitle(), scheme);
        }
        else {
            // new client
            log.debug("'{}' has no client in cache", getTitle());

            Pair<String, ProtocolClient> protocolClient = initNewClientFor(schemes);
            scheme = protocolClient.first();
            client = protocolClient.second();

            log.debug("'{}' got new client for '{}'", getTitle(), scheme);
            clients.put(scheme, client);
        }

        // find right operation and corresponding scheme in the array form
        Form form = null;
        for (Form f : forms) {
            if (f.getOp() != null && f.getOp().contains(op) && f.getHrefScheme().equals(scheme)) {
                form = f;
                break;
            }
        }

        if (form == null) {
            // if there no op was defined use default assignment
            Optional<Form> nonOpForm = forms.stream().filter(f -> f.getOp() == null || f.getOp().isEmpty()).findFirst();
            if (nonOpForm.isPresent()) {
                form = nonOpForm.get();
            }
            else {
                throw new NoFormForInteractionConsumedThingException(getTitle(), op);
            }
        }

        return new Pair(client, form);
    }

    private Pair<String, ProtocolClient> initNewClientFor(Set<String> schemes) throws ConsumedThingException {
        try {
            for (String scheme : schemes) {
                ProtocolClient client = servient.getClientFor(scheme);
                if (client != null) {
                    // init client's security system
                    List<String> security = getSecurity();
                    if (!security.isEmpty()) {
                        log.debug("'{}' setting credentials for '{}'", getTitle(), client);
                        Map<String, SecurityScheme> securityDefinitions = getSecurityDefinitions();
                        List<SecurityScheme> metadata = security.stream().map(securityDefinitions::get)
                                .filter(Objects::nonNull).collect(Collectors.toList());

                        client.setSecurity(metadata, servient.getCredentials(id));
                    }

                    return new Pair<>(scheme, client);
                }
            }

            throw new NoClientFactoryForSchemesConsumedThingException(getTitle(), schemes);
        }
        catch (ProtocolClientException e) {
            throw new ConsumedThingException("Unable to create client: " + e.getMessage());
        }
    }

    /**
     * Returns the values of all properties.
     *
     * @return
     */
    public CompletableFuture<Map<String, Object>> readProperties() {
        try {
            Pair<ProtocolClient, Form> clientAndForm = getClientFor(getForms(), Operation.READ_ALL_PROPERTIES);
            ProtocolClient client = clientAndForm.first();
            Form form = clientAndForm.second();

            log.debug("'{}' reading '{}'", getTitle(), form.getHref());

            CompletableFuture<Content> result = client.readResource(form);
            return result.thenApply(content -> {
                try {
                    return ContentManager.contentToValue(content, new ObjectSchema());
                }
                catch (ContentCodecException e) {
                    throw new CompletionException(new ConsumedThingException("Received invalid writeResource from Thing: " + e.getMessage()));
                }
            });
        }
        catch (ConsumedThingException e) {
            return CompletableFuture.failedFuture(e);
        }
    }

    /**
     * Returns the values of the properties contained in <code>names</code>.
     *
     * @param names
     *
     * @return
     */
    public CompletableFuture<Map<String, Object>> readProperties(List<String> names) {
        // TODO: read only requested properties
        return readProperties().thenApply(values -> {
            Stream<Map.Entry<String, Object>> stream = values.entrySet().stream().filter(e -> names.contains(e.getKey()));
            return stream.collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
        });
    }

    /**
     * Creates new form (if needed) for URI Variables
     * http://192.168.178.24:8080/counter/actions/increment{?step} with '{'step' : 3}' -&gt; http://192.168.178.24:8080/counter/actions/increment?step=3.<br>
     * see RFC6570 (https://tools.ietf.org/html/rfc6570) for URI Template syntax
     */
    public static Form handleUriVariables(Form form, Map<String, Object> parameters) {
        String href = form.getHref();
        UriTemplate uriTemplate = UriTemplate.fromTemplate(href);
        String updatedHref = uriTemplate.expand(parameters);

        if (!href.equals(updatedHref)) {
            // "clone" form to avoid modifying original form
            Form updatedForm = new Form.Builder(form)
                    .setHref(updatedHref)
                    .build();
            log.debug("'{}' update URI to '{}'", href, updatedHref);
            return updatedForm;
        }

        return form;
    }
}
