package city.sane.wot.thing.property;

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

import static java.util.concurrent.CompletableFuture.completedFuture;
import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;

public class ConsumedThingPropertyTest {
    private ThingProperty property;
    private ConsumedThing thing;
    private Form form;
    private ProtocolClient client;
    private Observer observer;

    @Before
    public void setUp() {
        property = mock(ThingProperty.class);
        thing = mock(ConsumedThing.class);
        form = mock(Form.class);
        client = mock(ProtocolClient.class);
        observer = mock(Observer.class);
    }

    @Test
    public void normalizeAbsoluteHref() {
        when(property.getForms()).thenReturn(List.of(form));
        when(form.getHref()).thenReturn("http://example.com/properties/count");

        ConsumedThingProperty consumedThingProperty = new ConsumedThingProperty("myProperty", property, thing);

        assertEquals("http://example.com/properties/count", consumedThingProperty.getForms().get(0).getHref());
    }

    @Test
    public void normalizeRelativeHref() {
        when(thing.getBase()).thenReturn("http://example.com");
        when(property.getForms()).thenReturn(List.of(form));
        when(form.getHref()).thenReturn("/properties/count");

        ConsumedThingProperty consumedThingProperty = new ConsumedThingProperty("myProperty", property, thing);

        assertEquals("http://example.com/properties/count", consumedThingProperty.getForms().get(0).getHref());
    }

    @Test
    public void normalizeAbsoluteHrefWithBase() {
        when(thing.getBase()).thenReturn("http://example.com");
        when(property.getForms()).thenReturn(List.of(form));
        when(form.getHref()).thenReturn("http://example.com/properties/count");

        ConsumedThingProperty consumedThingProperty = new ConsumedThingProperty("myProperty", property, thing);

        assertEquals("http://example.com/properties/count", consumedThingProperty.getForms().get(0).getHref());
    }

    @Test
    public void read() throws ConsumedThingException {
        when(thing.getClientFor(any(List.class), any())).thenReturn(new Pair(client, form));
        when(client.readResource(any())).thenReturn(completedFuture(null));

        ConsumedThingProperty consumedThingProperty = new ConsumedThingProperty("myProperty", property, thing);
        consumedThingProperty.read();

        verify(client, times(1)).readResource(any());
    }

    @Test
    public void write() throws ConsumedThingException {
        when(thing.getClientFor(any(List.class), any())).thenReturn(new Pair(client, form));
        when(client.writeResource(any(), any())).thenReturn(completedFuture(null));

        ConsumedThingProperty consumedThingProperty = new ConsumedThingProperty("myProperty", property, thing);
        consumedThingProperty.write("123");

        verify(client, times(1)).writeResource(any(), any());
    }

    @Test
    public void subscribe() throws ConsumedThingException, ProtocolClientNotImplementedException {
        when(thing.getClientFor(any(List.class), any())).thenReturn(new Pair(client, form));
        when(client.writeResource(any(), any())).thenReturn(completedFuture(null));

        ConsumedThingProperty consumedThingProperty = new ConsumedThingProperty("myProperty", property, thing);
        consumedThingProperty.subscribe(observer);

        verify(client, times(1)).subscribeResource(any(), any(), any(), any());
    }
}