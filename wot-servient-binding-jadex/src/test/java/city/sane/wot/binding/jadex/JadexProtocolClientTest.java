package city.sane.wot.binding.jadex;

import city.sane.wot.thing.Thing;
import city.sane.wot.thing.filter.ThingFilter;
import io.reactivex.rxjava3.annotations.NonNull;
import jadex.bridge.IComponentStep;
import jadex.bridge.IExternalAccess;
import jadex.bridge.IInternalAccess;
import jadex.commons.future.IFunctionalIntermediateFinishedListener;
import jadex.commons.future.IFunctionalIntermediateResultListener;
import jadex.commons.future.IFunctionalResultListener;
import jadex.commons.future.IFuture;
import jadex.commons.future.ITerminableIntermediateFuture;
import org.junit.Before;
import org.junit.Test;

import java.util.List;

import static org.hamcrest.CoreMatchers.hasItem;
import static org.junit.Assert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class JadexProtocolClientTest {
    private IExternalAccess platform;
    private JadexProtocolClient client;
    private ThingFilter filter;
    private IInternalAccess internalPlatform;
    private ITerminableIntermediateFuture<Object> searchFuture;
    private ThingService thingService;
    private IFuture<String> thingServiceFuture;

    @Before
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
            IFunctionalIntermediateResultListener resultListener = invocationOnMock.getArgument(0, IFunctionalIntermediateResultListener.class);
            IFunctionalIntermediateFinishedListener finishedListener = invocationOnMock.getArgument(1, IFunctionalIntermediateFinishedListener.class);
            resultListener.intermediateResultAvailable(thingService);
            finishedListener.finished();
            return null;
        }).when(searchFuture).addIntermediateResultListener(any(), any(), any());
        when(thingService.get()).thenReturn(thingServiceFuture);
        doAnswer(invocationOnMock -> {
            IFunctionalResultListener resultListener = invocationOnMock.getArgument(0, IFunctionalResultListener.class);
            resultListener.resultAvailable("{\"id\":\"counter\",\"title\":\"Zähler\"}");
            return null;
        }).when(thingServiceFuture).addResultListener(any(), any());

        client = new JadexProtocolClient(platform);
        @NonNull List<Thing> things = client.discover(filter).toList().blockingGet();

        assertThat(things, hasItem(new Thing.Builder().setId("counter").setTitle("Zähler").build()));
    }
}