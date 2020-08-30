package city.sane.wot.thing.event;

import city.sane.Pair;
import city.sane.wot.binding.ProtocolClient;
import city.sane.wot.binding.ProtocolClientException;
import city.sane.wot.thing.ConsumedThing;
import city.sane.wot.thing.ConsumedThingException;
import city.sane.wot.thing.form.Form;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.Observer;
import org.junit.Before;
import org.junit.Test;

import java.util.List;

import static org.mockito.Mockito.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class ConsumedThingEventTest {
    private ThingEvent<Object> event;
    private ConsumedThing thing;
    private ProtocolClient client;
    private Form form;
    private Observer observer;
    private Observable observable;

    @Before
    public void setUp() {
        event = mock(ThingEvent.class);
        thing = mock(ConsumedThing.class);
        client = mock(ProtocolClient.class);
        form = mock(Form.class);
        observer = mock(Observer.class);
        observable = mock(Observable.class);
    }

    @Test
    public void subscribeShouldCallUnderlyingClient() throws ConsumedThingException, ProtocolClientException {
        when(thing.getClientFor(any(List.class), any())).thenReturn(new Pair(client, form));
        when(form.getHref()).thenReturn("test:/myAction");
        when(client.observeResource(any())).thenReturn(observable);

        ConsumedThingEvent<Object> consumedThingEvent = new ConsumedThingEvent<Object>("myEvent", event, thing);
        consumedThingEvent.observer().subscribe(observer);

        verify(client).observeResource(any());
    }
}