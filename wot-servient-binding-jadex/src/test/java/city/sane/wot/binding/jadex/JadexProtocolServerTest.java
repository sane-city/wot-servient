package city.sane.wot.binding.jadex;

import city.sane.RefCountResource;
import city.sane.wot.thing.ExposedThing;
import jadex.bridge.IExternalAccess;
import jadex.commons.future.IFuture;
import org.junit.Before;
import org.junit.Test;

import java.util.Map;

import static org.mockito.Mockito.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class JadexProtocolServerTest {
    private ExposedThing thing;
    private ThingsService thingsService;
    private RefCountResource<IExternalAccess> platformProvider;
    private IExternalAccess platform;
    private Map<String, ExposedThing> things;

    @Before
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