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

import city.sane.Pair;
import city.sane.wot.binding.ProtocolClient;
import city.sane.wot.binding.ProtocolClientException;
import city.sane.wot.content.ContentManager;
import city.sane.wot.thing.ConsumedThing;
import city.sane.wot.thing.ConsumedThingException;
import city.sane.wot.thing.form.Form;
import city.sane.wot.thing.form.Operation;
import io.reactivex.rxjava3.core.Observable;

import java.util.Objects;
import java.util.Optional;

/**
 * Used in combination with {@link ConsumedThing} and allows consuming of a {@link ThingEvent}.
 */
public class ConsumedThingEvent<T> extends ThingEvent<T> {
    private final String name;
    private final ConsumedThing thing;

    public ConsumedThingEvent(String name, ThingEvent<T> event, ConsumedThing thing) {
        this.name = name;
        forms = event.getForms();
        type = event.getType();
        data = event.getData();
        this.thing = thing;
    }

    public Observable<Optional<T>> observer() throws ConsumedThingException {
        try {
            Pair<ProtocolClient, Form> clientAndForm = thing.getClientFor(getForms(), Operation.SUBSCRIBE_EVENT);

            ProtocolClient client = clientAndForm.first();
            Form form = clientAndForm.second();

            return client.observeResource(form)
                    .map(content -> Optional.ofNullable(ContentManager.contentToValue(content, getData())));
        }
        catch (ProtocolClientException e) {
            throw new ConsumedThingException(e);
        }
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), name, thing);
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
        ConsumedThingEvent<?> that = (ConsumedThingEvent<?>) o;
        return Objects.equals(name, that.name) &&
                Objects.equals(thing, that.thing);
    }

    @Override
    public String toString() {
        return "ConsumedThingEvent{" +
                "name='" + name + '\'' +
                ", data=" + data +
                ", type='" + type + '\'' +
                ", description='" + description + '\'' +
                ", descriptions=" + descriptions +
                ", forms=" + forms +
                ", uriVariables=" + uriVariables +
                '}';
    }
}
