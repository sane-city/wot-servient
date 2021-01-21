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
package city.sane.wot.binding.websocket;

import city.sane.wot.Servient;
import city.sane.wot.thing.ExposedThing;
import city.sane.wot.thing.action.ExposedThingAction;
import city.sane.wot.thing.event.ExposedThingEvent;
import city.sane.wot.thing.property.ExposedThingProperty;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.EventLoopGroup;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Map;

import static org.mockito.Mockito.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class WebsocketProtocolServerTest {
    private ServerBootstrap serverBootstrap;
    private EventLoopGroup serverBossGroup;
    private EventLoopGroup serverWorkerGroup;
    private Map<String, ExposedThing> things;
    private List<String> addresses;
    private int bindPort;
    private Channel serverChannel;
    private Servient servient;
    private ExposedThing thing;
    private ExposedThingProperty<Object> property;
    private ExposedThingEvent<Object> event;
    private ExposedThingAction<Object, Object> action;

    @BeforeEach
    public void setUp() {
        serverBootstrap = mock(ServerBootstrap.class);
        serverBossGroup = mock(EventLoopGroup.class);
        serverWorkerGroup = mock(EventLoopGroup.class);
        things = mock(Map.class);
        addresses = mock(List.class);
        bindPort = 80;
        serverChannel = mock(Channel.class);
        servient = mock(Servient.class);
        thing = mock(ExposedThing.class);
        property = mock(ExposedThingProperty.class);
        event = mock(ExposedThingEvent.class);
        action = mock(ExposedThingAction.class);
    }

    @Test
    public void startShouldBindToCorrectPort() {
        WebsocketProtocolServer server = new WebsocketProtocolServer(serverBootstrap, serverBossGroup, serverWorkerGroup, things, addresses, bindPort, null);
        server.start(servient);

        verify(serverBootstrap, timeout(1 * 1000L)).bind(80);
    }

    @Test
    public void stopShouldCloseServer() {
        WebsocketProtocolServer server = new WebsocketProtocolServer(serverBootstrap, serverBossGroup, serverWorkerGroup, things, addresses, bindPort, serverChannel);
        server.stop();

        verify(serverChannel, timeout(1 * 1000L)).close();
    }

    @Test
    public void exposeShouldAddFormsToThing() {
        when(thing.getId()).thenReturn("counter");
        when(thing.getProperties()).thenReturn(Map.of("count", property));
        when(property.isObservable()).thenReturn(true);
        when(thing.getActions()).thenReturn(Map.of("reset", action));
        when(thing.getEvents()).thenReturn(Map.of("changed", event));

        WebsocketProtocolServer server = new WebsocketProtocolServer(serverBootstrap, serverBossGroup, serverWorkerGroup, things, List.of("ws://localhost"), bindPort, serverChannel);
        server.expose(thing);

        verify(property, timeout(1 * 1000L).times(3)).addForm(any());
        verify(action, timeout(1 * 1000L)).addForm(any());
        verify(event, timeout(1 * 1000L)).addForm(any());
    }

    @Test
    public void destroyShouldRemoveThing() {
        when(thing.getId()).thenReturn("counter");

        WebsocketProtocolServer server = new WebsocketProtocolServer(serverBootstrap, serverBossGroup, serverWorkerGroup, things, List.of("ws://localhost"), bindPort, serverChannel);
        server.destroy(thing);

        verify(things, timeout(1 * 1000L)).remove("counter");
    }
}