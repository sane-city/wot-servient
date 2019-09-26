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
    final static Logger log = LoggerFactory.getLogger(ExposedThingEvent.class);

    private final String name;
    private final ExposedThing thing;
    @JsonIgnore
    private final EventState state = new EventState();

    public ExposedThingEvent(String name, ThingEvent event, ExposedThing thing) {
        this.name = name;
        this.description = event.getDescription();
        this.descriptions = event.getDescriptions();
        this.uriVariables = event.getUriVariables();
        this.type = event.getType();
        this.data = event.getData();
        this.thing = thing;
    }

    public EventState getState() {
        return state;
    }

    public CompletableFuture<Void> emit(Object data) {
        log.info("Event '{}' has been emitted", name);
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
