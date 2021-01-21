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
package city.sane.wot.binding.jadex;

import city.sane.wot.thing.ExposedThing;
import jadex.bridge.IExternalAccess;
import jadex.bridge.IInternalAccess;
import jadex.commons.future.IFuture;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class ThingsAgentTest {
    private IInternalAccess ia;
    private ExposedThing thing;
    private Map<String, IExternalAccess> children;
    private Map<String, ExposedThing> things;
    private IExternalAccess externalAccess;

    @BeforeEach
    public void setup() {
        ia = mock(IInternalAccess.class);
        thing = mock(ExposedThing.class);
        things = mock(Map.class);
        children = mock(Map.class);
        externalAccess = mock(IExternalAccess.class);
    }

    @Test
    public void createdShouldNotFail() {
        ThingsAgent agent = new ThingsAgent(ia, Map.of("counter", thing), children);
        agent.created();

        // shot not fail
        assertTrue(true);
    }

    @Test
    public void exposeShouldCreateThingAgent() {
        when(ia.createComponent(any())).thenReturn(mock(IFuture.class));

        ThingsAgent agent = new ThingsAgent(ia, things, children);

        agent.expose("counter");

        // CreateionInfo ist not comparable
//        CreationInfo info = new CreationInfo()
//                .setFilenameClass(ThingAgent.class)
//                .addArgument("thing", thing);
        verify(ia).createComponent(any());
    }

    @Test
    public void destroyShouldDestroyThingAgent() {
        when(children.get(any())).thenReturn(externalAccess);
        when(externalAccess.killComponent()).thenReturn(mock(IFuture.class));

        ThingsAgent agent = new ThingsAgent(ia, things, children);
        agent.destroy("counter");

        verify(externalAccess).killComponent();
    }
}