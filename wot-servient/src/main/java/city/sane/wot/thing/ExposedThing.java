package city.sane.wot.thing;

import city.sane.wot.Servient;
import city.sane.wot.thing.action.ExposedThingAction;
import city.sane.wot.thing.action.ThingAction;
import city.sane.wot.thing.event.ExposedThingEvent;
import city.sane.wot.thing.event.ThingEvent;
import city.sane.wot.thing.form.Form;
import city.sane.wot.thing.observer.Observer;
import city.sane.wot.thing.observer.Subject;
import city.sane.wot.thing.observer.Subscribable;
import city.sane.wot.thing.observer.Subscription;
import city.sane.wot.thing.property.ExposedThingProperty;
import city.sane.wot.thing.property.ThingProperty;
import city.sane.wot.thing.security.SecurityScheme;
import com.fasterxml.jackson.annotation.JsonIgnore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * This the server API that allows defining request handlers, properties, actions, and events
 * to a Thing. An ExposedThing is created by the {@link city.sane.wot.Wot#produce(Thing)} method.
 * https://w3c.github.io/wot-scripting-api/#the-exposedthing-interface
 */
public class ExposedThing extends Thing<ExposedThingProperty, ExposedThingAction, ExposedThingEvent> implements Subscribable<Object> {
    private static final Logger log = LoggerFactory.getLogger(ExposedThing.class);

    private final Servient servient;

    @JsonIgnore
    private final Subject subject;

    public ExposedThing(Servient servient) {
        this.servient = servient;
        subject = new Subject();
    }

    public ExposedThing(Servient servient, Thing thing) {
        this(servient);
        setObjectType(thing.getObjectType());
        setObjectContexts(thing.getObjectContext());
        setId(thing.getId());
        setTitle(thing.getTitle());
        setTitles(thing.getTitles());
        setDescription(thing.getDescription());
        setDescriptions(thing.getDescriptions());
        setForms(thing.getForms());
        setSecurity(thing.getSecurity());
        setSecurityDefinitions(thing.getSecurityDefinitions());
        setBase(thing.getBase());
        ((Map<String, ThingProperty>) thing.getProperties()).forEach(this::addProperty);
        ((Map<String, ThingAction>) thing.getActions()).forEach(this::addAction);
        ((Map<String, ThingEvent>) thing.getEvents()).forEach(this::addEvent);
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
     * Defines the JSON-LD datatype.
     *
     * @param objectType
     *
     * @return
     */
    private ExposedThing setObjectType(String objectType) {
        this.objectType = objectType;
        return this;
    }

    /**
     * Defines the JSON-LD contexts
     *
     * @param objectContexts
     *
     * @return
     */
    private ExposedThing setObjectContexts(Context objectContexts) {
        objectContext = objectContexts;
        return this;
    }

    public ExposedThing setId(String id) {
        this.id = id;
        return this;
    }

    public ExposedThing setTitle(String title) {
        this.title = title;
        return this;
    }

    private ExposedThing setTitles(Map<String, String> titles) {
        this.titles = titles;
        return this;
    }

    public ExposedThing setDescription(String description) {
        this.description = description;
        return this;
    }

    private ExposedThing setDescriptions(Map<String, String> descriptions) {
        this.descriptions = descriptions;
        return this;
    }

    public ExposedThing addForm(Form form) {
        forms.add(form);
        return this;
    }

    private ExposedThing setForms(List<Form> forms) {
        this.forms = forms;
        return this;
    }

    /**
     * Specifies the security mechanisms supported by the Thing.<br>
     * See also: https://www.w3.org/TR/wot-thing-description/#security-serialization-json
     *
     * @param security
     *
     * @return
     */
    private ExposedThing setSecurity(List<String> security) {
        this.security = security;
        return this;
    }

    /**
     * Describes properties of the security mechanisms listed in {@link #security} (e.g. password authentication).<br>
     * See also: https://www.w3.org/TR/wot-thing-description/#security-serialization-json
     *
     * @param securityDefinitions
     *
     * @return
     */
    private ExposedThing setSecurityDefinitions(Map<String, SecurityScheme> securityDefinitions) {
        this.securityDefinitions = securityDefinitions;
        return this;
    }

    /**
     * Defines a base URL. This allows the use of relative URLs in the forms (see {@link Form#getHref()}). Since most URLs are only different in the path, this
     * can shorten the Thing Description.
     *
     * @param base
     * @return
     */
    private ExposedThing setBase(String base) {
        this.base = base;
        return this;
    }

    /**
     * Returns the subject of this thing. Can be used to subscribe to Thing changes.
     *
     * @return
     */
    public Subject getSubject() {
        return subject;
    }

    @Override
    public Subscription subscribe(Observer<Object> observer) {
        return subject.subscribe(observer);
    }

    /**
     * Adds the given <code>property</code> with the given <code>name</code> to the Thing.<br>
     * <code>readHandler</code> is invoked when the property is read. It returns a future with the value of the property. Set to <code>null</code> if not
     * needed.<br>
     * <code>writeHandler</code> is invoked when the property is written to. It consumes the new property value and returns the output of the write operation.
     * Set to <code>null</code> if not needed.<br>
     *
     * @param name
     * @param property
     * @param readHandler
     * @param writeHandler
     *
     * @return
     */
    public ExposedThing addProperty(String name, ThingProperty property, Supplier<CompletableFuture<Object>> readHandler, Function<Object, CompletableFuture<Object>> writeHandler) {
        log.info("'{}' adding Property '{}'", getId(), name);

        ExposedThingProperty exposedProperty = new ExposedThingProperty(name, property, this);
        exposedProperty.getState().setReadHandler(readHandler);
        exposedProperty.getState().setWriteHandler(writeHandler);
        properties.put(name, exposedProperty);

        return this;
    }

    /**
     * Adds the given <code>property</code> with the given <code>name</code> to the Thing.
     *
     * @param name
     * @param property
     *
     * @return
     */
    public ExposedThing addProperty(String name, ThingProperty property) {
        return addProperty(name, property, null, null);
    }

    /**
     * Adds the given <code>property</code> with the given <code>name</code> to the Thing. <code>init</code> is used as the initial value of the property.
     * <code>readHandler</code> is invoked when the property is read. It returns a future with the value of the property. Set to <code>null</code> if not
     * needed.<br>
     * <code>writeHandler</code> is invoked when the property is written to. It consumes the new property value and returns the output of the write operation.
     * Set to <code>null</code> if not needed.<br>
     *
     * @param name
     * @param property
     * @param init
     *
     * @return
     */
    private ExposedThing addProperty(String name,
                                     ThingProperty property,
                                     Supplier<CompletableFuture<Object>> readHandler,
                                     Function<Object, CompletableFuture<Object>> writeHandler,
                                     Object init) {
        addProperty(name, property, readHandler, writeHandler);

        ExposedThingProperty exposedProperty = properties.get(name);
        try {
            // wait until init value has been written
            exposedProperty.write(init).get();
        }
        catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        catch (ExecutionException e) {
            log.warn("'{}' unable to write initial value for Property '{}': {}", getId(), name, e.getMessage());
        }

        return this;
    }

    /**
     * Adds the given <code>property</code> with the given <code>name</code> to the Thing. <code>init</code> is used as the initial value of the property.
     *
     * @param name
     * @param property
     * @param init
     *
     * @return
     */
    public ExposedThing addProperty(String name, ThingProperty property, Object init) {
        return addProperty(name, property, null, null, init);
    }

    /**
     * Adds a property with the given <code>name</code> to the Thing.
     *
     * @param name
     *
     * @return
     */
    public ExposedThing addProperty(String name) {
        return addProperty(name, new ThingProperty());
    }

    /**
     * Removes the property with the given <code>name</code> from the Thing.
     *
     * @param name
     *
     * @return
     */
    public ExposedThing removeProperty(String name) {
        log.info("'{}' removing Property '{}'", getId(), name);
        properties.remove(name);
        return this;
    }

    /**
     * Adds the given <code>action</code> with the given <code>name</code> to the Thing. <code>handler</code> is invoked when the action is called.
     * This method can be used if the handler needs parameters to call and has a return value.
     * The contents of the parameters are described in {@link ExposedThingAction#invoke(Object, Map)}.
     *
     * @param name
     * @param handler
     *
     * @return
     */
    public ExposedThing addAction(String name, ThingAction action, BiFunction<Object, Map<String, Object>, CompletableFuture<Object>> handler) {
        log.info("'{}' adding Action '{}'", getId(), name);

        ExposedThingAction exposedAction = new ExposedThingAction(name, action, this);
        exposedAction.getState().setHandler(handler);
        actions.put(name, exposedAction);

        return this;
    }

    /**
     * Adds the given <code>action</code> with the given <code>name</code> to the Thing. <code>handler</code> is invoked when the action is called.
     * This method can be used if the handler needs parameters to call and has no return value.
     * The contents of the parameters are described in {@link ExposedThingAction#invoke(Object, Map)}.
     *
     * @param name
     * @param handler
     *
     * @return
     */
    public ExposedThing addAction(String name, ThingAction action, BiConsumer<Object, Map<String, Object>> handler) {
        return addAction(name, action, (input, options) -> {
            handler.accept(input, options);
            return CompletableFuture.completedFuture(null);
        });
    }

    /**
     * Adds the given <code>action</code> with the given <code>name</code> to the Thing. <code>handler</code> is invoked when the action is called.
     * This method can be used if the <code>handler</code> does not require any parameters for the call has no return value.
     *
     * @param name
     * @param action
     * @param handler
     *
     * @return
     */
    public ExposedThing addAction(String name, ThingAction action, Runnable handler) {
        return addAction(name, action, (BiConsumer<Object, Map<String, Object>>) (input, options) -> handler.run());
    }

    /**
     * Adds the given <code>action</code> with the given <code>name</code> to the Thing. <code>handler</code> is invoked when the action is called.
     * This method can be used if the <code>handler</code> does not require any parameters for the call and only returns a value.
     *
     * @param name
     * @param action
     * @param handler
     *
     * @return
     */
    public ExposedThing addAction(String name, ThingAction action, Supplier<CompletableFuture<Object>> handler) {
        return addAction(name, action, (BiFunction<Object, Map<String, Object>, CompletableFuture<Object>>) (input, options) -> handler.get());
    }

    /**
     * Adds an action with the given <code>name</code> to the Thing. <code>handler</code> is invoked when the action is called.
     * This method can be used if the handler needs parameters to call and has a return value.
     * The contents of the parameters are described in {@link ExposedThingAction#invoke(Object, Map)}.
     *
     * @param name
     * @param handler
     *
     * @return
     */
    public ExposedThing addAction(String name, BiFunction<Object, Map<String, Object>, CompletableFuture<Object>> handler) {
        return addAction(name, new ThingAction(), handler);
    }

    /**
     * Adds an action with the given <code>name</code> to the Thing. <code>handler</code> is invoked when the action is called.
     * This method can be used if the handler needs parameters to call and has no return value.
     * The contents of the parameters are described in {@link ExposedThingAction#invoke(Object, Map)}.
     *
     * @param name
     * @param handler
     *
     * @return
     */
    public ExposedThing addAction(String name, BiConsumer<Object, Map<String, Object>> handler) {
        return addAction(name, new ThingAction(), handler);
    }

    /**
     * Adds an action with the given <code>name</code> to the Thing. <code>handler</code> is invoked when the action is called.
     * This method can be used if the <code>handler</code> does not require any parameters for the call has no return value.
     *
     * @param name
     * @param handler
     *
     * @return
     */
    public ExposedThing addAction(String name, Runnable handler) {
        return addAction(name, new ThingAction(), handler);
    }

    /**
     * Adds an with the given <code>name</code> to the Thing. <code>handler</code> is invoked when the action is called.
     * This method can be used if the <code>handler</code> does not require any parameters for the call and only returns a value.
     *
     * @param name
     * @param handler
     *
     * @return
     */
    public ExposedThing addAction(String name, Supplier<CompletableFuture<Object>> handler) {
        return addAction(name, new ThingAction(), handler);
    }

    /**
     * Adds the given <code>action</code> with the given <code>name</code> to the Thing.
     *
     * @param name
     * @param action
     *
     * @return
     */
    private ExposedThing addAction(String name, ThingAction action) {
        return addAction(name, action, () -> {
        });
    }

    /**
     * Adds an action with the given <code>name</code> to the Thing.
     *
     * @param name
     *
     * @return
     */
    public ExposedThing addAction(String name) {
        return addAction(name, new ThingAction());
    }

    /**
     * Removes the action with the given <code>name</code> from the Thing.
     *
     * @param name
     *
     * @return
     */
    public ExposedThing removeAction(String name) {
        log.info("'{}' removing Action '{}'", getId(), name);
        actions.remove(name);
        return this;
    }

    /**
     * Adds the given <code>event</code> with the given <code>name</code> to the Thing.
     *
     * @param name
     * @param event
     *
     * @return
     */
    public ExposedThing addEvent(String name, ThingEvent event) {
        ExposedThingEvent exposedEvent = new ExposedThingEvent(name, event);
        events.put(name, exposedEvent);

        return this;
    }

    /**
     * Adds an event with the given <code>name</code> to the Thing.
     *
     * @param name
     *
     * @return
     */
    public ExposedThing addEvent(String name) {
        return addEvent(name, new ThingEvent());
    }

    /**
     * Removes the event with the given <code>name</code> from the Thing.
     *
     * @param name
     *
     * @return
     */
    public ExposedThing removeEvent(String name) {
        log.info("'{}' removing Event '{}'", getId(), name);
        events.remove(name);
        return this;
    }

    /**
     * Start serving external requests for the Thing, so that WoT Interactions using Properties, Actions and Events will
     * be possible. The TD will be extended by the Interaction Endpoints.
     *
     * @return
     */
    public CompletableFuture<ExposedThing> expose() {
        log.info("Expose all Interactions and TD for '{}'", getId());

        // let servient forward exposure to the servers
        return servient.expose(getId()).whenComplete((thing, e) -> {
            if (thing != null) {
                // inform TD observers
                subject.next(thing);
            }
        });
    }

    /**
     * Stop serving external requests for the Thing. The interaction endpoints are removed from the TD.
     *
     * @return
     */
    public CompletableFuture<ExposedThing> destroy() {
        log.info("Stop exposing all Interactions and TD for '{}'", getId());

        // let servient forward destroy to the servers
        return servient.destroy(getId()).whenComplete((thing, e) -> {
            if (thing != null) {
                // inform TD observers
                subject.next(thing);
            }
        });
    }

    /**
     * Returns a {@link Map} with property names as map key and property values as map value.
     *
     * @return
     */
    public CompletableFuture<Map<String, Object>> readProperties() {
        // read all properties async
        List<CompletableFuture> futures = new ArrayList<>();

        Map<String, Object> values = new HashMap<>();
        getProperties().forEach((name, property) -> {
            CompletableFuture<Object> readFuture = property.read();
            CompletableFuture<Object> putValuesFuture = readFuture.thenApply(value -> values.put(name, value));
            futures.add(putValuesFuture);
        });

        // wait until all properties have been read
        return CompletableFuture.allOf(futures.toArray(CompletableFuture[]::new)).thenApply(f -> values);
    }

    /**
     * Writes the transferred <code>values</code> to the respective properties and returns the new value of the respective properties.
     *
     * @param values
     *
     * @return
     */
    public CompletableFuture<Map<String, Object>> writeProperties(Map<String, Object> values) {
        // write properties async
        List<CompletableFuture> futures = new ArrayList<>();

        Map<String, Object> returnValues = new HashMap<>();
        values.forEach((name, value) -> {
            ExposedThingProperty property = getProperty(name);
            if (property != null) {
                CompletableFuture<Object> future = property.write(value);
                futures.add(future);
                future.whenComplete((returnValue, e) -> returnValues.put(name, value));
            }
        });

        // wait until all properties have been written
        return CompletableFuture.allOf(futures.toArray(CompletableFuture[]::new)).thenApply(f -> returnValues);
    }
}
