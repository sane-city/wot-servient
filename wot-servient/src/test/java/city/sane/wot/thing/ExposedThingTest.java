package city.sane.wot.thing;

import city.sane.wot.Servient;
import city.sane.wot.ServientException;
import city.sane.wot.thing.action.ExposedThingAction;
import city.sane.wot.thing.event.ExposedThingEvent;
import city.sane.wot.thing.observer.Subject;
import city.sane.wot.thing.property.ExposedThingProperty;
import com.github.jsonldjava.shaded.com.google.common.base.Supplier;
import org.junit.Before;
import org.junit.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.function.BiConsumer;
import java.util.function.Function;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class ExposedThingTest {
    private Servient servient;
    private Subject subject;
    private String objectType;
    private Context objectContext;
    private String id;
    private String title;
    private String description;
    private String base;
    private ExposedThingProperty property;
    private ExposedThingAction action;
    private ExposedThingEvent event;
    private Supplier<CompletableFuture<Object>> readHandler;
    private Function<Object, CompletableFuture<Object>> writeHandler;
    private BiConsumer biConsumerHandler;
    private Runnable runnableHandler;
    private Supplier supplierHandler;

    @Before
    public void setUp() {
        servient = mock(Servient.class);
        subject = mock(Subject.class);
        objectType = "";
        objectContext = mock(Context.class);
        id = "count";
        title = "counter";
        description = "";
        base = "";
        property = mock(ExposedThingProperty.class);
        action = mock(ExposedThingAction.class);
        event = mock(ExposedThingEvent.class);
        readHandler = mock(Supplier.class);
        writeHandler = mock(Function.class);
        biConsumerHandler = mock(BiConsumer.class);
        runnableHandler = mock(Runnable.class);
        supplierHandler = mock(Supplier.class);
    }

    @Test
    public void getProperty() {
        Map<String, ExposedThingProperty> properties = Map.of("count", property);
        ExposedThing exposedThing = new ExposedThing(servient, subject, objectType, objectContext, id, title, Map.of(), description, Map.of(), List.of(), List.of(), Map.of(), base, properties, Map.of(), Map.of());

        assertEquals(property, exposedThing.getProperty("count"));
    }

    @Test
    public void getAction() {
        Map<String, ExposedThingAction> actions = Map.of("increment", action);
        ExposedThing exposedThing = new ExposedThing(servient, subject, objectType, objectContext, id, title, Map.of(), description, Map.of(), List.of(), List.of(), Map.of(), base, Map.of(), actions, Map.of());

        assertEquals(action, exposedThing.getAction("increment"));
    }

    @Test
    public void getEvent() {
        Map<String, ExposedThingEvent> events = Map.of("change", event);
        ExposedThing exposedThing = new ExposedThing(servient, subject, objectType, objectContext, id, title, Map.of(), description, Map.of(), List.of(), List.of(), Map.of(), base, Map.of(), Map.of(), events);

        assertEquals(event, exposedThing.getEvent("change"));
    }

    @Test
    public void readProperties() throws ExecutionException, InterruptedException {
        when(property.read()).thenReturn(CompletableFuture.completedFuture(null));
        Map<String, ExposedThingProperty> properties = Map.of("count", property);
        ExposedThing exposedThing = new ExposedThing(servient, subject, objectType, objectContext, id, title, Map.of(), description, Map.of(), List.of(), List.of(), Map.of(), base, properties, Map.of(), Map.of());

        exposedThing.readProperties().get();

        verify(property, times(1)).read();
    }

    @Test
    public void writeProperties() throws ExecutionException, InterruptedException {
        when(property.write(any())).thenReturn(CompletableFuture.completedFuture(null));
        Map<String, ExposedThingProperty> properties = Map.of("count", property);
        ExposedThing exposedThing = new ExposedThing(servient, subject, objectType, objectContext, id, title, Map.of(), description, Map.of(), List.of(), List.of(), Map.of(), base, properties, Map.of(), Map.of());

        exposedThing.writeProperties(Map.of("count", 0)).get();

        verify(property, times(1)).write(0);
    }

    @Test
    public void expose() {
        when(servient.expose(any())).thenReturn(CompletableFuture.completedFuture(null));
        ExposedThing exposedThing = new ExposedThing(servient, subject, objectType, objectContext, id, title, Map.of(), description, Map.of(), List.of(), List.of(), Map.of(), base, Map.of(), Map.of(), Map.of());

        exposedThing.expose();

        verify(servient, times(1)).expose(id);
    }

    @Test
    public void destroy() throws ExecutionException, InterruptedException, ServientException {
        when(servient.destroy(any())).thenReturn(CompletableFuture.completedFuture(null));
        ExposedThing exposedThing = new ExposedThing(servient, subject, objectType, objectContext, id, title, Map.of(), description, Map.of(), List.of(), List.of(), Map.of(), base, Map.of(), Map.of(), Map.of());

        exposedThing.destroy();

        verify(servient, times(1)).destroy(id);
    }

    @Test
    public void addProperty() {
        ExposedThing exposedThing = new ExposedThing(servient, subject, objectType, objectContext, id, title, Map.of(), description, Map.of(), List.of(), List.of(), Map.of(), base, new HashMap(), Map.of(), Map.of());

        exposedThing.addProperty("count");

        assertNotNull(exposedThing.getProperty("count"));
    }

    @Test
    public void addPropertyWithHandlers() {
        ExposedThing exposedThing = new ExposedThing(servient, subject, objectType, objectContext, id, title, Map.of(), description, Map.of(), List.of(), List.of(), Map.of(), base, new HashMap(), Map.of(), Map.of());

        exposedThing.addProperty("count", property, readHandler, writeHandler);
        ExposedThingProperty property = exposedThing.getProperty("count");

        assertNotNull(property);
        assertEquals(readHandler, property.getState().getReadHandler());
        assertEquals(writeHandler, property.getState().getWriteHandler());
    }

    @Test
    public void addPropertyWithInitValue() {
        when(property.write(any())).thenReturn(CompletableFuture.completedFuture(null));
        ExposedThing exposedThing = new ExposedThing(servient, subject, objectType, objectContext, id, title, Map.of(), description, Map.of(), List.of(), List.of(), Map.of(), base, new HashMap(), Map.of(), Map.of());

        exposedThing.addProperty("count", property, 1337);

        assertNotNull(exposedThing.getProperty("count"));
    }

    @Test
    public void removeProperty() {
        Map<String, ExposedThingProperty> properties = new HashMap(Map.of("count", property));
        ExposedThing exposedThing = new ExposedThing(servient, subject, objectType, objectContext, id, title, Map.of(), description, Map.of(), List.of(), List.of(), Map.of(), base, properties, Map.of(), Map.of());

        exposedThing.removeProperty("count");

        assertNull(exposedThing.getProperty("count"));
    }

    @Test
    public void addAction() {
        ExposedThing exposedThing = new ExposedThing(servient, subject, objectType, objectContext, id, title, Map.of(), description, Map.of(), List.of(), List.of(), Map.of(), base, Map.of(), new HashMap(), Map.of());

        exposedThing.addAction("increment");

        assertNotNull(exposedThing.addAction("increment"));
    }

    @Test
    public void addActionWithBiConsumerHandler() {
        ExposedThing exposedThing = new ExposedThing(servient, subject, objectType, objectContext, id, title, Map.of(), description, Map.of(), List.of(), List.of(), Map.of(), base, Map.of(), new HashMap(), Map.of());

        exposedThing.addAction("increment", biConsumerHandler);
        ExposedThingAction action = exposedThing.getAction("increment");

        assertNotNull(action);
    }

    @Test
    public void addActionWithRunnableHandler() {
        ExposedThing exposedThing = new ExposedThing(servient, subject, objectType, objectContext, id, title, Map.of(), description, Map.of(), List.of(), List.of(), Map.of(), base, Map.of(), new HashMap(), Map.of());

        exposedThing.addAction("increment", runnableHandler);
        ExposedThingAction action = exposedThing.getAction("increment");

        assertNotNull(action);
    }

    @Test
    public void addActionWithSupplierHandler() throws ServientException, ExecutionException, InterruptedException {
        ExposedThing exposedThing = new ExposedThing(servient, subject, objectType, objectContext, id, title, Map.of(), description, Map.of(), List.of(), List.of(), Map.of(), base, Map.of(), new HashMap(), Map.of());

        exposedThing.addAction("increment", supplierHandler);
        ExposedThingAction action = exposedThing.getAction("increment");

        assertNotNull(action);
    }

    @Test
    public void removeAction() {
        Map<String, ExposedThingAction> actions = new HashMap(Map.of("increment", action));
        ExposedThing exposedThing = new ExposedThing(servient, subject, objectType, objectContext, id, title, Map.of(), description, Map.of(), List.of(), List.of(), Map.of(), base, Map.of(), actions, Map.of());

        exposedThing.removeAction("increment");

        assertNull(exposedThing.getAction("increment"));
    }

    @Test
    public void addEvent() {
        ExposedThing exposedThing = new ExposedThing(servient, subject, objectType, objectContext, id, title, Map.of(), description, Map.of(), List.of(), List.of(), Map.of(), base, Map.of(), Map.of(), new HashMap());

        exposedThing.addEvent("change");

        assertNotNull(exposedThing.addEvent("change"));
    }

    @Test
    public void removeEvent() {
        Map<String, ExposedThingEvent> events = new HashMap(Map.of("change", event));
        ExposedThing exposedThing = new ExposedThing(servient, subject, objectType, objectContext, id, title, Map.of(), description, Map.of(), List.of(), List.of(), Map.of(), base, Map.of(), Map.of(), events);

        exposedThing.removeEvent("change");

        assertNull(exposedThing.getEvent("change"));
    }

    @Test
    public void testEquals() {
        ExposedThing thingA = new ExposedThing(null, new Thing.Builder().setId("counter").build());
        ExposedThing thingB = new ExposedThing(null, new Thing.Builder().setId("counter").build());

        assertEquals(thingA, thingB);
    }
}
