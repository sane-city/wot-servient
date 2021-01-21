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
package city.sane.wot.thing.property;

import city.sane.Pair;
import city.sane.wot.binding.ProtocolClient;
import city.sane.wot.binding.ProtocolClientException;
import city.sane.wot.content.Content;
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

import static java.util.concurrent.CompletableFuture.completedFuture;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class ConsumedThingPropertyTest {
    private ThingProperty<Object> property;
    private ConsumedThing thing;
    private Form form;
    private ProtocolClient client;
    private Observer observer;
    private Observable<Content> observable;

    @BeforeEach
    public void setUp() {
        property = mock(ThingProperty.class);
        thing = mock(ConsumedThing.class);
        form = mock(Form.class);
        client = mock(ProtocolClient.class);
        observer = mock(Observer.class);
        observable = mock(Observable.class);
    }

    @Test
    public void normalizeAbsoluteHref() {
        when(property.getForms()).thenReturn(List.of(form));
        when(form.getHref()).thenReturn("http://example.com/properties/count");

        ConsumedThingProperty<Object> consumedThingProperty = new ConsumedThingProperty<Object>("myProperty", property, thing);

        assertEquals("http://example.com/properties/count", consumedThingProperty.getForms().get(0).getHref());
    }

    @Test
    public void normalizeRelativeHref() {
        when(thing.getBase()).thenReturn("http://example.com");
        when(property.getForms()).thenReturn(List.of(form));
        when(form.getHref()).thenReturn("/properties/count");

        ConsumedThingProperty<Object> consumedThingProperty = new ConsumedThingProperty<Object>("myProperty", property, thing);

        assertEquals("http://example.com/properties/count", consumedThingProperty.getForms().get(0).getHref());
    }

    @Test
    public void normalizeAbsoluteHrefWithBase() {
        when(thing.getBase()).thenReturn("http://example.com");
        when(property.getForms()).thenReturn(List.of(form));
        when(form.getHref()).thenReturn("http://example.com/properties/count");

        ConsumedThingProperty<Object> consumedThingProperty = new ConsumedThingProperty<Object>("myProperty", property, thing);

        assertEquals("http://example.com/properties/count", consumedThingProperty.getForms().get(0).getHref());
    }

    @Test
    public void readShouldCallUnderlyingClient() throws ConsumedThingException {
        when(thing.getClientFor(any(List.class), any())).thenReturn(new Pair(client, form));
        when(client.readResource(any())).thenReturn(completedFuture(null));

        ConsumedThingProperty<Object> consumedThingProperty = new ConsumedThingProperty<Object>("myProperty", property, thing);
        consumedThingProperty.read();

        verify(client).readResource(any());
    }

    @Test
    public void writeShouldCallUnderlyingClient() throws ConsumedThingException {
        when(thing.getClientFor(any(List.class), any())).thenReturn(new Pair(client, form));
        when(client.writeResource(any(), any())).thenReturn(completedFuture(null));

        ConsumedThingProperty<Object> consumedThingProperty = new ConsumedThingProperty<Object>("myProperty", property, thing);
        consumedThingProperty.write("123");

        verify(client).writeResource(any(), any());
    }

    @Test
    public void subscribeShoulCallUnderlyingClient() throws ConsumedThingException, ProtocolClientException {
        when(thing.getClientFor(any(List.class), any())).thenReturn(new Pair(client, form));
        when(client.observeResource(any())).thenReturn(observable);

        ConsumedThingProperty<Object> consumedThingProperty = new ConsumedThingProperty<Object>("myProperty", property, thing);
        consumedThingProperty.observer().subscribe(observer);

        verify(client).observeResource(any());
    }
}