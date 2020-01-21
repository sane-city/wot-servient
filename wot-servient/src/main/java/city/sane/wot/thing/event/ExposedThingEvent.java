package city.sane.wot.thing.event;

import city.sane.wot.thing.ExposedThing;
import city.sane.wot.thing.observer.Observer;
import city.sane.wot.thing.observer.Subscribable;
import city.sane.wot.thing.observer.Subscription;
import com.fasterxml.jackson.annotation.JsonIgnore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.CompletableFuture;

/**
 * Used in combination with {@link ExposedThing} and allows exposing of a {@link ThingEvent}.
 */
public class ExposedThingEvent extends ThingEvent implements Subscribable<Object> {
    private static final Logger log = LoggerFactory.getLogger(ExposedThingEvent.class);

    private final String name;
    @JsonIgnore
    private final EventState state = new EventState();

    public ExposedThingEvent(String name, ThingEvent event) {
        this.name = name;
        description = event.getDescription();
        descriptions = event.getDescriptions();
        uriVariables = event.getUriVariables();
        type = event.getType();
        data = event.getData();
    }

    @Override
    public int hashCode() {
        return super.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        return super.equals(obj);
    }

    public EventState getState() {
        return state;
    }

    public CompletableFuture<Void> emit(Object data) {
        log.debug("Event '{}' has been emitted", name);
        return state.getSubject().next(data);
    }

    public CompletableFuture<Void> emit() {
        return emit(null);
    }

    @Override
    public Subscription subscribe(Observer<Object> observer) {
        return state.getSubject().subscribe(observer);
    }
}
