package city.sane.wot.thing.event;

import city.sane.Pair;
import city.sane.wot.binding.ProtocolClient;
import city.sane.wot.binding.ProtocolClientNotImplementedException;
import city.sane.wot.thing.ConsumedThing;
import city.sane.wot.thing.ConsumedThingException;
import city.sane.wot.thing.form.Form;
import city.sane.wot.thing.observer.Observer;
import org.junit.Before;
import org.junit.Test;

import java.util.List;
import java.util.concurrent.CompletableFuture;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;

public class ConsumedThingEventTest {
    private ThingEvent event;
    private ConsumedThing thing;
    private ProtocolClient client;
    private Form form;
    private Observer observer;

    @Before
    public void setUp() {
        event = mock(ThingEvent.class);
        thing = mock(ConsumedThing.class);
        client = mock(ProtocolClient.class);
        form = mock(Form.class);
        observer = mock(Observer.class);
    }

    @Test
    public void subscribe() throws ConsumedThingException, ProtocolClientNotImplementedException {
        when(thing.getClientFor(any(List.class), any())).thenReturn(new Pair(client, form));
        when(form.getHref()).thenReturn("test:/myAction");
        when(client.subscribeResource(any(), any())).thenReturn(CompletableFuture.completedFuture(null));

        ConsumedThingEvent consumedThingEvent = new ConsumedThingEvent("myEvent", event, thing);
        consumedThingEvent.subscribe(observer);

        verify(client, times(1)).subscribeResource(any(), any());

    }
}