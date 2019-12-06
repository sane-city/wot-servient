package city.sane.wot.thing.property;

import city.sane.wot.thing.ExposedThing;
import city.sane.wot.thing.observer.Observer;
import city.sane.wot.thing.observer.Subscribable;
import city.sane.wot.thing.observer.Subscription;
import com.fasterxml.jackson.annotation.JsonIgnore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;
import java.util.concurrent.CompletableFuture;

/**
 * Used in combination with {@link ExposedThing} and allows exposing of a {@link ThingProperty}.
 */
public class ExposedThingProperty extends ThingProperty implements Subscribable<Object> {
    static final Logger log = LoggerFactory.getLogger(ExposedThingProperty.class);

    private final String name;
    private final ExposedThing thing;
    @JsonIgnore
    private final PropertyState state = new PropertyState();

    public ExposedThingProperty(String name, ThingProperty property, ExposedThing thing) {
        this.name = name;

        if (property != null) {
            this.objectType = property.getObjectType();
            this.description = property.getDescription();
            this.descriptions = property.getDescriptions();
            this.type = property.getType();
            this.observable = property.isObservable();
            this.readOnly = property.isReadOnly();
            this.writeOnly = property.isWriteOnly();
            this.uriVariables = property.getUriVariables();
            this.optionalProperties = property.getOptionalProperties();
        }

        this.thing = thing;
    }

    public CompletableFuture<Object> read() {
        // call read handler (if any)
        if (state.getReadHandler() != null) {
            log.info("'{}' calls registered readHandler for Property '{}'", thing.getId(), name);

            // update internal state in case writeHandler wants to get the value
            return state.getReadHandler().get().whenComplete((customValue, e) -> state.setValue(customValue));
        }
        else {
            CompletableFuture<Object> future = new CompletableFuture<>();

            Object value = state.getValue();
            log.info("'{}' gets internal value '{}' for Property '{}'", thing.getId(), value, name);
            future.complete(value);

            return future;
        }
    }

    public CompletableFuture<Object> write(Object value) {
        // call write handler (if any)
        if (state.getWriteHandler() != null) {
            log.info("'{}' calls registered writeHandler for Property '{}'", thing.getId(), name);

            return state.getWriteHandler().apply(value).whenComplete((customValue, e) -> {
                log.info("'{}' write handler for Property '{}' sets custom value '{}'", thing.getId(), name, customValue);
                if (!Objects.equals(state.getValue(), customValue)) {
                    state.setValue(customValue);

                    // inform property observers
                    state.getSubject().next(customValue);
                }
            });
        }
        else {
            if (!Objects.equals(state.getValue(), value)) {
                log.info("'{}' sets Property '{}' to internal value '{}'", thing.getId(), name, value);
                state.setValue(value);

                // inform property observers
                state.getSubject().next(value);
            }

            return CompletableFuture.completedFuture(null);
        }
    }

    @Override
    public Subscription subscribe(Observer<Object> observer) {
        log.info("'{}' subscribe to Property '{}'", thing.getId(), name);
        return state.getSubject().subscribe(observer);
    }

    public PropertyState getState() {
        return state;
    }
}
