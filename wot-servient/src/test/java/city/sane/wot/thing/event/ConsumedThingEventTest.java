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
import city.sane.wot.thing.ConsumedThing;
import city.sane.wot.thing.ConsumedThingException;
import city.sane.wot.thing.form.Form;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.Observer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.mockito.Mockito.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class ConsumedThingEventTest {
    private ThingEvent<Object> event;
    private ConsumedThing thing;
    private ProtocolClient client;
    private Form form;
    private Observer observer;
    private Observable observable;

    @BeforeEach
    public void setUp() {
        event = mock(ThingEvent.class);
        thing = mock(ConsumedThing.class);
        client = mock(ProtocolClient.class);
        form = mock(Form.class);
        observer = mock(Observer.class);
        observable = mock(Observable.class);
    }

    @Test
    public void subscribeShouldCallUnderlyingClient() throws ConsumedThingException, ProtocolClientException {
        when(thing.getClientFor(any(List.class), any())).thenReturn(new Pair(client, form));
        when(client.observeResource(any())).thenReturn(observable);

        ConsumedThingEvent<Object> consumedThingEvent = new ConsumedThingEvent<Object>("myEvent", event, thing);
        consumedThingEvent.observer().subscribe(observer);

        verify(client).observeResource(any());
    }
}