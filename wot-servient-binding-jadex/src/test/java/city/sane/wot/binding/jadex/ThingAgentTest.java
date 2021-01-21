package city.sane.wot.binding.jadex;

import city.sane.wot.thing.ExposedThing;
import city.sane.wot.thing.action.ExposedThingAction;
import city.sane.wot.thing.event.ExposedThingEvent;
import city.sane.wot.thing.property.ExposedThingProperty;
import jadex.bridge.IInternalAccess;
import jadex.bridge.service.IService;
import jadex.bridge.service.IServiceIdentifier;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static java.util.concurrent.CompletableFuture.completedFuture;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.withSettings;

public class ThingAgentTest {
    private ExposedThing thing;
    private IInternalAccess ia;
    private ExposedThingProperty<Object> property;
    private ExposedThingAction<Object, Object> action;
    private ExposedThingEvent<Object> event;
    private ThingService thingService;
    private JadexContent jadexContent;

    @BeforeEach
    public void setup() {
        thing = mock(ExposedThing.class);
        ia = mock(IInternalAccess.class);
        property = mock(ExposedThingProperty.class);
        action = mock(ExposedThingAction.class);
        event = mock(ExposedThingEvent.class);
        thingService = mock(ThingService.class, withSettings().extraInterfaces(IService.class));
        jadexContent = mock(JadexContent.class);
    }

    @Test
    public void createdShouldAddForms() {
        when(ia.getProvidedService(ThingService.class)).thenReturn(thingService);
        when(((IService) thingService).getServiceId()).thenReturn(mock(IServiceIdentifier.class));
        when(thing.getId()).thenReturn("counter");
        when(thing.getProperties()).thenReturn(Map.of("count", property));
        when(property.isObservable()).thenReturn(true);
        when(thing.getActions()).thenReturn(Map.of("reset", action));
        when(thing.getEvents()).thenReturn(Map.of("changed", event));

        ThingAgent agent = new ThingAgent(ia, thing);
        agent.created();

        verify(thing, timeout(1 * 1000L)).addForm(any());
        verify(property, timeout(1 * 1000L).times(1)).addForm(any()); // observable is currently not supported by the jadex binding
        verify(action, timeout(1 * 1000L)).addForm(any());
        verify(event, timeout(1 * 1000L)).addForm(any());
    }

    @Test
    public void killedShouldThrowNoError() {
        ThingAgent agent = new ThingAgent(ia, thing);
        agent.killed();

        // shot not fail
        assertTrue(true);
    }

    @Test
    public void getShouldReturnThingDescription() {
        ThingAgent agent = new ThingAgent(ia, thing);
        agent.get();

        verify(thing, timeout(1 * 1000L)).toJson();
    }

    @Test
    public void readPropertiesShouldReturnMapWithAllPropertiesValues() {
        when(thing.readProperties()).thenReturn(completedFuture(null));

        ThingAgent agent = new ThingAgent(ia, thing);
        agent.readProperties();

        verify(thing, timeout(1 * 1000L)).readProperties();
    }

    @Test
    public void readPropertyShouldReturnValueOfGivenProperty() {
        when(thing.getProperty("count")).thenReturn(property);
        when(property.read()).thenReturn(completedFuture(null));

        ThingAgent agent = new ThingAgent(ia, thing);
        agent.readProperty("count");

        verify(property, timeout(1 * 1000L)).read();
    }

    @Test
    public void writePropertyShouldWriteToGivenProperty() {
        when(thing.getProperty("count")).thenReturn(property);
        when(property.write(any())).thenReturn(completedFuture(null));

        ThingAgent agent = new ThingAgent(ia, thing);
        agent.writeProperty("count", jadexContent);

        verify(property, timeout(1 * 1000L)).write(any());
    }
}