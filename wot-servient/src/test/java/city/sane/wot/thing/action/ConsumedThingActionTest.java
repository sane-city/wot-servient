package city.sane.wot.thing.action;

import city.sane.Pair;
import city.sane.wot.binding.ProtocolClient;
import city.sane.wot.thing.ConsumedThing;
import city.sane.wot.thing.ConsumedThingException;
import city.sane.wot.thing.form.Form;
import org.junit.Before;
import org.junit.Test;

import java.util.List;

import static java.util.concurrent.CompletableFuture.completedFuture;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class ConsumedThingActionTest {
    private ThingAction<Object, Object> action;
    private ConsumedThing thing;
    private ProtocolClient client;
    private Form form;

    @Before
    public void setUp() {
        action = mock(ThingAction.class);
        thing = mock(ConsumedThing.class);
        client = mock(ProtocolClient.class);
        form = mock(Form.class);
    }

    @Test
    public void invokeShouldCallUnderlyingClient() throws ConsumedThingException {
        when(thing.getClientFor(any(List.class), any())).thenReturn(new Pair(client, form));
        when(form.getHref()).thenReturn("test:/myAction");
        when(client.invokeResource(any(), any())).thenReturn(completedFuture(null));

        ConsumedThingAction consumedThingAction = new ConsumedThingAction("myAction", action, thing);
        consumedThingAction.invoke();

        verify(client).invokeResource(any(), any());
    }
}