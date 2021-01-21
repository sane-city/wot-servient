package city.sane.wot.thing;

import city.sane.wot.Servient;
import city.sane.wot.ServientException;
import city.sane.wot.thing.action.ExposedThingAction;
import city.sane.wot.thing.event.ExposedThingEvent;
import city.sane.wot.thing.property.ExposedThingProperty;
import com.github.jsonldjava.shaded.com.google.common.base.Supplier;
import io.reactivex.rxjava3.subjects.Subject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.function.BiConsumer;
import java.util.function.Function;

import static java.util.concurrent.CompletableFuture.completedFuture;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class ExposedThingTest {
    private Servient servient;
    private Subject subject;
    private String objectType;
    private Context objectContext;
    private String id;
    private String title;
    private String description;
    private String base;
    private ExposedThingProperty<Object> property;
    private ExposedThingAction<Object, Object> action;
    private ExposedThingEvent<Object> event;
    private Supplier<CompletableFuture<Object>> readHandler;
    private Function<Object, CompletableFuture<Object>> writeHandler;
    private BiConsumer biConsumerHandler;
    private Runnable runnableHandler;
    private Supplier supplierHandler;

    @BeforeEach
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
        Map<String, ExposedThingProperty<Object>> properties = Map.of("count", property);
        ExposedThing exposedThing = new ExposedThing(servient, subject, objectType, objectContext, id, title, Map.of(), description, Map.of(), List.of(), List.of(), Map.of(), base, properties, Map.of(), Map.of());

        assertEquals(property, exposedThing.getProperty("count"));
    }

    @Test
    public void getAction() {
        Map<String, ExposedThingAction<Object, Object>> actions = Map.of("increment", action);
        ExposedThing exposedThing = new ExposedThing(servient, subject, objectType, objectContext, id, title, Map.of(), description, Map.of(), List.of(), List.of(), Map.of(), base, Map.of(), actions, Map.of());

        assertEquals(action, exposedThing.getAction("increment"));
    }

    @Test
    public void getEvent() {
        Map<String, ExposedThingEvent<Object>> events = Map.of("change", event);
        ExposedThing exposedThing = new ExposedThing(servient, subject, objectType, objectContext, id, title, Map.of(), description, Map.of(), List.of(), List.of(), Map.of(), base, Map.of(), Map.of(), events);

        assertEquals(event, exposedThing.getEvent("change"));
    }

    @Test
    public void readProperties() throws ExecutionException, InterruptedException {
        when(property.read()).thenReturn(completedFuture(null));
        Map<String, ExposedThingProperty<Object>> properties = Map.of("count", property);
        ExposedThing exposedThing = new ExposedThing(servient, subject, objectType, objectContext, id, title, Map.of(), description, Map.of(), List.of(), List.of(), Map.of(), base, properties, Map.of(), Map.of());

        exposedThing.readProperties().get();

        verify(property).read();
    }

    @Test
    public void writeProperties() throws ExecutionException, InterruptedException {
        when(property.write(any())).thenReturn(completedFuture(null));
        Map<String, ExposedThingProperty<Object>> properties = Map.of("count", property);
        ExposedThing exposedThing = new ExposedThing(servient, subject, objectType, objectContext, id, title, Map.of(), description, Map.of(), List.of(), List.of(), Map.of(), base, properties, Map.of(), Map.of());

        exposedThing.writeProperties(Map.of("count", 0)).get();

        verify(property).write(0);
    }

    @Test
    public void expose() {
        when(servient.expose(any())).thenReturn(completedFuture(null));
        ExposedThing exposedThing = new ExposedThing(servient, subject, objectType, objectContext, id, title, Map.of(), description, Map.of(), List.of(), List.of(), Map.of(), base, Map.of(), Map.of(), Map.of());

        exposedThing.expose();

        verify(servient).expose(id);
    }

    @Test
    public void destroy() throws ExecutionException, InterruptedException, ServientException {
        when(servient.destroy(any())).thenReturn(completedFuture(null));
        ExposedThing exposedThing = new ExposedThing(servient, subject, objectType, objectContext, id, title, Map.of(), description, Map.of(), List.of(), List.of(), Map.of(), base, Map.of(), Map.of(), Map.of());

        exposedThing.destroy();

        verify(servient).destroy(id);
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
        ExposedThingProperty<Object> property = exposedThing.getProperty("count");

        assertNotNull(property);
        assertEquals(readHandler, property.getState().getReadHandler());
        assertEquals(writeHandler, property.getState().getWriteHandler());
    }

    @Test
    public void addPropertyWithInitValue() {
        when(property.write(any())).thenReturn(completedFuture(null));
        ExposedThing exposedThing = new ExposedThing(servient, subject, objectType, objectContext, id, title, Map.of(), description, Map.of(), List.of(), List.of(), Map.of(), base, new HashMap(), Map.of(), Map.of());

        exposedThing.addProperty("count", property, 1337);

        assertNotNull(exposedThing.getProperty("count"));
    }

    @Test
    public void removeProperty() {
        Map<String, ExposedThingProperty<Object>> properties = new HashMap(Map.of("count", property));
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
        ExposedThingAction<Object, Object> action = exposedThing.getAction("increment");

        assertNotNull(action);
    }

    @Test
    public void addActionWithRunnableHandler() {
        ExposedThing exposedThing = new ExposedThing(servient, subject, objectType, objectContext, id, title, Map.of(), description, Map.of(), List.of(), List.of(), Map.of(), base, Map.of(), new HashMap(), Map.of());

        exposedThing.addAction("increment", runnableHandler);
        ExposedThingAction<Object, Object> action = exposedThing.getAction("increment");

        assertNotNull(action);
    }

    @Test
    public void addActionWithSupplierHandler() throws ServientException, ExecutionException, InterruptedException {
        ExposedThing exposedThing = new ExposedThing(servient, subject, objectType, objectContext, id, title, Map.of(), description, Map.of(), List.of(), List.of(), Map.of(), base, Map.of(), new HashMap(), Map.of());

        exposedThing.addAction("increment", supplierHandler);
        ExposedThingAction<Object, Object> action = exposedThing.getAction("increment");

        assertNotNull(action);
    }

    @Test
    public void removeAction() {
        Map<String, ExposedThingAction<Object, Object>> actions = new HashMap(Map.of("increment", action));
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
        Map<String, ExposedThingEvent<Object>> events = new HashMap(Map.of("change", event));
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
