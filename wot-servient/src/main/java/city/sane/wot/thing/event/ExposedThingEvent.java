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
package city.sane.wot.thing.event;

import city.sane.wot.thing.ExposedThing;
import com.fasterxml.jackson.annotation.JsonIgnore;
import io.reactivex.rxjava3.core.Observable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;
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

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), name, state);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        if (!super.equals(o)) {
            return false;
        }
        ExposedThingEvent<?> that = (ExposedThingEvent<?>) o;
        return Objects.equals(name, that.name) &&
                Objects.equals(state, that.state);
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
}
