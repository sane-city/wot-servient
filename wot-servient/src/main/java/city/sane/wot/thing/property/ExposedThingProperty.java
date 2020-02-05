package city.sane.wot.thing.property;

import city.sane.wot.thing.ExposedThing;
import city.sane.wot.thing.observer.Observer;
import city.sane.wot.thing.observer.Subscribable;
import city.sane.wot.thing.observer.Subscription;
import com.fasterxml.jackson.annotation.JsonIgnore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;

import static java.util.concurrent.CompletableFuture.completedFuture;
import static java.util.concurrent.CompletableFuture.failedFuture;

/**
 * Used in combination with {@link ExposedThing} and allows exposing of a {@link ThingProperty}.
 */
public class ExposedThingProperty extends ThingProperty implements Subscribable<Object> {
    private static final Logger log = LoggerFactory.getLogger(ExposedThingProperty.class);

    private final String name;
    private final ExposedThing thing;
    @JsonIgnore
    private final PropertyState state;

    public ExposedThingProperty(String name, ExposedThing thing, PropertyState state, String objectType, String description, Map<String, String> descriptions, String type, boolean observable, boolean readOnly, boolean writeOnly, Map<String, Map> uriVariables, Map<String, Object> optionalProperties) {
        this.name = name;
        this.thing = thing;
        this.state = state;
        this.objectType = objectType;
        this.description = description;
        this.descriptions = descriptions;
        this.type = type;
        this.observable = observable;
        this.readOnly = readOnly;
        this.writeOnly = writeOnly;
        this.uriVariables = uriVariables;
        this.optionalProperties = optionalProperties;
    }

    public ExposedThingProperty(String name, ThingProperty property, ExposedThing thing) {
        this.name = name;
        this.thing = thing;
        state = new PropertyState();

        if (property != null) {
            objectType = property.getObjectType();
            description = property.getDescription();
            descriptions = property.getDescriptions();
            type = property.getType();
            observable = property.isObservable();
            readOnly = property.isReadOnly();
            writeOnly = property.isWriteOnly();
            uriVariables = property.getUriVariables();
            optionalProperties = property.getOptionalProperties();
        }
    }

    @Override
    public boolean equals(Object obj) {
        return super.equals(obj);
    }

    @Override
    public int hashCode() {
        return super.hashCode();
    }

    public CompletableFuture<Object> read() {
        // call read handler (if any)
        if (state.getReadHandler() != null) {
            log.debug("'{}' calls registered readHandler for Property '{}'", thing.getId(), name);

            // update internal state in case writeHandler wants to get the value
            try {
                return state.getReadHandler().get().whenComplete((customValue, e) -> state.setValue(customValue));
            }
            catch (Exception e) {
                return failedFuture(e);
            }
        }
        else {
            CompletableFuture<Object> future = new CompletableFuture<>();

            Object value = state.getValue();
            log.debug("'{}' gets internal value '{}' for Property '{}'", thing.getId(), value, name);
            future.complete(value);

            return future;
        }
    }

    public CompletableFuture<Object> write(Object value) {
        // call write handler (if any)
        if (state.getWriteHandler() != null) {
            log.debug("'{}' calls registered writeHandler for Property '{}'", thing.getId(), name);

            try {
                return state.getWriteHandler().apply(value).whenComplete((customValue, e) -> {
                    log.debug("'{}' write handler for Property '{}' sets custom value '{}'", thing.getId(), name, customValue);
                    if (!Objects.equals(state.getValue(), customValue)) {
                        state.setValue(customValue);

                        // inform property observers
                        state.getSubject().next(customValue);
                    }
                });
            }
            catch (Exception e) {
                return failedFuture(e);
            }
        }
        else {
            if (!Objects.equals(state.getValue(), value)) {
                log.debug("'{}' sets Property '{}' to internal value '{}'", thing.getId(), name, value);
                state.setValue(value);

                // inform property observers
                state.getSubject().next(value);
            }

            return completedFuture(null);
        }
    }

    @Override
    public Subscription subscribe(Observer<Object> observer) {
        log.debug("'{}' subscribe to Property '{}'", thing.getId(), name);
        return state.getSubject().subscribe(observer);
    }

    public PropertyState getState() {
        return state;
    }

    @Override
    public String toString() {
        return "ExposedThingProperty{" +
                "name='" + name + '\'' +
                ", state=" + state +
                ", objectType='" + objectType + '\'' +
                ", type='" + type + '\'' +
                ", observable=" + observable +
                ", readOnly=" + readOnly +
                ", writeOnly=" + writeOnly +
                ", optionalProperties=" + optionalProperties +
                ", description='" + description + '\'' +
                ", descriptions=" + descriptions +
                ", forms=" + forms +
                ", uriVariables=" + uriVariables +
                '}';
    }
}
