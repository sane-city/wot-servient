package city.sane.wot.binding.jadex;

import city.sane.wot.thing.ExposedThing;
import jadex.bridge.IExternalAccess;
import jadex.bridge.IInternalAccess;
import jadex.commons.future.IFuture;
import org.junit.Before;
import org.junit.Test;

import java.util.Map;

import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class ThingsAgentTest {
    private IInternalAccess ia;
    private ExposedThing thing;
    private Map<String, IExternalAccess> children;
    private Map<String, ExposedThing> things;
    private IExternalAccess externalAccess;

    @Before
    public void setup() {
        ia = mock(IInternalAccess.class);
        thing = mock(ExposedThing.class);
        things = mock(Map.class);
        children = mock(Map.class);
        externalAccess = mock(IExternalAccess.class);
    }

    @Test
    public void createdShouldNotFail() {
        when(ia.createComponent(any())).thenReturn(mock(IFuture.class));

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