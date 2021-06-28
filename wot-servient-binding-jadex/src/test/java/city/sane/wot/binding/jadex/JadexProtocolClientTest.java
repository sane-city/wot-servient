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

import city.sane.wot.thing.Thing;
import city.sane.wot.thing.filter.ThingFilter;
import io.reactivex.rxjava3.annotations.NonNull;
import jadex.bridge.IComponentStep;
import jadex.bridge.IExternalAccess;
import jadex.bridge.IInternalAccess;
import jadex.commons.future.IFuture;
import jadex.commons.future.IResultListener;
import jadex.commons.future.ITerminableIntermediateFuture;
import jadex.commons.future.IntermediateDefaultResultListener;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.hamcrest.CoreMatchers.hasItem;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class JadexProtocolClientTest {
    private IExternalAccess platform;
    private JadexProtocolClient client;
    private ThingFilter filter;
    private IInternalAccess internalPlatform;
    private ITerminableIntermediateFuture<Object> searchFuture;
    private ThingService thingService;
    private IFuture<String> thingServiceFuture;

    @BeforeEach
    public void setUp() {
        platform = mock(IExternalAccess.class);
        filter = mock(ThingFilter.class);
        internalPlatform = mock(IInternalAccess.class);
        searchFuture = mock(ITerminableIntermediateFuture.class);
        thingService = mock(ThingService.class);
        thingServiceFuture = mock(IFuture.class);
    }

    @Test
    public void discoverShouldReturnFoundThings() {
        when(platform.scheduleStep(any())).then(invocationOnMock -> {
            IComponentStep step = invocationOnMock.getArgument(0, IComponentStep.class);
            step.execute(internalPlatform);
            return null;
        });
        when(internalPlatform.searchServices(any())).thenReturn(searchFuture);
        doAnswer(invocationOnMock -> {
            IntermediateDefaultResultListener resultListener = invocationOnMock.getArgument(0, IntermediateDefaultResultListener.class);
            resultListener.intermediateResultAvailable(thingService);
            resultListener.finished();
            return null;
        }).when(searchFuture).addResultListener(any());
        when(thingService.get()).thenReturn(thingServiceFuture);
        doAnswer(invocationOnMock -> {
            IResultListener resultListener = invocationOnMock.getArgument(0, IResultListener.class);
            resultListener.resultAvailable("{\"id\":\"counter\",\"title\":\"Zähler\"}");
            return null;
        }).when(thingServiceFuture).addResultListener(any());

        client = new JadexProtocolClient(platform);
        @NonNull List<Thing> things = client.discover(filter).toList().blockingGet();

        assertThat(things, hasItem(new Thing.Builder().setId("counter").setTitle("Zähler").build()));
    }
}