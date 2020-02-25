package city.sane.wot.thing.event;

import city.sane.wot.thing.ExposedThing;
import com.fasterxml.jackson.annotation.JsonIgnore;
import io.reactivex.rxjava3.core.Observable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;

/**
 * Used in combination with {@link ExposedThing} and allows exposing of a {@link ThingEvent}.
 */
public class ExposedThingEvent<T> extends ThingEvent<T> {
    private static final Logger log = LoggerFactory.getLogger(ExposedThingEvent.class);
    private final String name;
    @JsonIgnore
    private final EventState<T> state;

    public ExposedThingEvent(String name, ThingEvent<T> event) {
        this(name, new EventState<>());
        description = event.getDescription();
        descriptions = event.getDescriptions();
        uriVariables = event.getUriVariables();
        type = event.getType();
        data = event.getData();
    }

    ExposedThingEvent(String name, EventState<T> state) {
        this.name = name;
        this.state = state;
    }

    @Override
    public int hashCode() {
        return super.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        return super.equals(obj);
    }

    @Override
    public String toString() {
        return "ExposedThingEvent{" +
                "name='" + name + '\'' +
                ", state=" + state +
                ", data=" + data +
                ", type='" + type + '\'' +
                ", description='" + description + '\'' +
                ", descriptions=" + descriptions +
                ", forms=" + forms +
                ", uriVariables=" + uriVariables +
                '}';
    }

    public EventState<T> getState() {
        return state;
    }

    public void emit() {
        emit(null);
    }

    public void emit(T data) {
        log.debug("Event '{}' has been emitted", name);
        state.getSubject().onNext(Optional.ofNullable(data));
    }

    public Observable<Optional<T>> observer() {
        return state.getSubject();
    }
}
