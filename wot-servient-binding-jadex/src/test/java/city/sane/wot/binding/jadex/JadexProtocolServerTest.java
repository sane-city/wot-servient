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

import city.sane.RefCountResource;
import city.sane.wot.thing.ExposedThing;
import jadex.bridge.IExternalAccess;
import jadex.commons.future.IFuture;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Map;

import static org.mockito.Mockito.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class JadexProtocolServerTest {
    private ExposedThing thing;
    private ThingsService thingsService;
    private RefCountResource<IExternalAccess> platformProvider;
    private IExternalAccess platform;
    private Map<String, ExposedThing> things;

    @BeforeEach
    public void setUp() {
        thing = mock(ExposedThing.class);
        thingsService = mock(ThingsService.class);
        platformProvider = mock(RefCountResource.class);
        platform = mock(IExternalAccess.class);
        things = mock(Map.class);
    }

    @Test
    public void exposeShouldInstructThingsAgentToExposeThing() {
        when(thing.getId()).thenReturn("counter");
        when(thingsService.expose(any())).thenReturn(mock(IFuture.class));

        JadexProtocolServer server = new JadexProtocolServer(platformProvider, platform, thingsService, things);
        server.expose(thing);

        verify(thingsService).expose("counter");
    }
}