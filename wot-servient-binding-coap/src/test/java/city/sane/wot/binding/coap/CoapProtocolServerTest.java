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
package city.sane.wot.binding.coap;

import city.sane.wot.Servient;
import city.sane.wot.binding.coap.resource.ThingResource;
import city.sane.wot.thing.ExposedThing;
import city.sane.wot.thing.action.ExposedThingAction;
import city.sane.wot.thing.event.ExposedThingEvent;
import city.sane.wot.thing.property.ExposedThingProperty;
import io.reactivex.rxjava3.subjects.PublishSubject;
import org.eclipse.californium.core.CoapResource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class CoapProtocolServerTest {
    private String bindHost;
    private int bindPort;
    private List<String> addresses;
    private Map<String, ExposedThing> things;
    private Map<String, CoapResource> resources;
    private Supplier<WotCoapServer> serverSupplier;
    private Servient servient;
    private WotCoapServer coapServer;
    private ExposedThing thing;
    private CoapResource resource;
    private ExposedThingProperty<Object> property;
    private ExposedThingAction<Object, Object> action;
    private ExposedThingEvent<Object> event;

    @BeforeEach
    public void setUp() {
        bindHost = "0.0.0.0";
        bindPort = 5683;
        addresses = mock(List.class);
        things = mock(Map.class);
        resources = mock(Map.class);
        serverSupplier = mock(Supplier.class);
        servient = mock(Servient.class);
        coapServer = mock(WotCoapServer.class);
        thing = mock(ExposedThing.class);
        resource = mock(CoapResource.class);
        property = mock(ExposedThingProperty.class);
        action = mock(ExposedThingAction.class);
        event = mock(ExposedThingEvent.class);
    }

    @Test
    public void startShouldCreateAndStartServer() {
        when(serverSupplier.get()).thenReturn(coapServer);

        CoapProtocolServer server = new CoapProtocolServer(bindHost, bindPort, addresses, things, resources, serverSupplier, null, bindPort, addresses);
        server.start(servient);

        verify(serverSupplier, timeout(1 * 1000L)).get();
        verify(coapServer, timeout(1 * 1000L)).start();
    }

    @Test
    public void stopShouldStopAndDestroyServer() {
        CoapProtocolServer server = new CoapProtocolServer(bindHost, bindPort, addresses, things, resources, serverSupplier, coapServer, bindPort, addresses);
        server.stop();

        verify(coapServer, timeout(1 * 1000L)).stop();
        verify(coapServer, timeout(1 * 1000L)).destroy();
    }

    @Test
    public void exposeShouldCreateThingResource() {
        when(thing.getId()).thenReturn("counter");

        CoapProtocolServer server = new CoapProtocolServer(bindHost, bindPort, addresses, things, resources, serverSupplier, coapServer, bindPort, addresses);
        server.expose(thing);

        verify(resources, timeout(1 * 1000L)).put(eq("counter"), any(ThingResource.class));
    }

    @Test
    public void exposeShouldAddFormsToThing() {
        when(coapServer.getRoot()).thenReturn(resource);
        when(thing.getId()).thenReturn("counter");
        when(thing.getProperties()).thenReturn(Map.of("count", property));
        when(property.isObservable()).thenReturn(true);
        when(property.observer()).thenReturn(PublishSubject.create());
        when(thing.getActions()).thenReturn(Map.of("reset", action));
        when(thing.getEvents()).thenReturn(Map.of("changed", event));
        when(event.observer()).thenReturn(PublishSubject.create());

        CoapProtocolServer server = new CoapProtocolServer(bindHost, bindPort, List.of("coap://localhost"), things, resources, serverSupplier, coapServer, bindPort, List.of("coap://localhost"));
        server.expose(thing);

        verify(property, timeout(1 * 1000L).times(2)).addForm(any());
        verify(action, timeout(1 * 1000L)).addForm(any());
        verify(event, timeout(1 * 1000L)).addForm(any());
    }

    @Test
    public void destroyShouldRemoveThingResource() {
        when(resources.remove(any())).thenReturn(resource);
        when(coapServer.getRoot()).thenReturn(resource);

        CoapProtocolServer server = new CoapProtocolServer(bindHost, bindPort, addresses, things, resources, serverSupplier, coapServer, bindPort, addresses);
        server.destroy(thing);

        verify(resource, timeout(1 * 1000L)).delete(resource);
    }

    @Test
    public void getDirectoryUrlShouldReturnFristAddress() throws URISyntaxException {
        when(addresses.get(0)).thenReturn("coap://0.0.0.0");

        CoapProtocolServer server = new CoapProtocolServer(bindHost, bindPort, addresses, things, resources, serverSupplier, coapServer, bindPort, addresses);

        assertEquals(new URI("coap://0.0.0.0"), server.getDirectoryUrl());
    }

    @Test
    public void getThingUrl() throws URISyntaxException {
        when(addresses.get(0)).thenReturn("coap://0.0.0.0");

        CoapProtocolServer server = new CoapProtocolServer(bindHost, bindPort, addresses, things, resources, serverSupplier, coapServer, bindPort, addresses);

        assertEquals(new URI("coap://0.0.0.0/counter"), server.getThingUrl("counter"));
    }
}