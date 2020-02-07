package city.sane.wot.thing.property;

import city.sane.wot.thing.ExposedThing;
import city.sane.wot.thing.observer.Subject;
import org.junit.Before;
import org.junit.Test;

import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.function.Function;
import java.util.function.Supplier;

import static java.util.concurrent.CompletableFuture.completedFuture;
import static org.mockito.Mockito.*;

public class ExposedThingPropertyTest {
    private ExposedThing thing;
    private String name;
    private ThingProperty property;
    private PropertyState state;
    private String objectType;
    private String description;
    private Map<String, String> descriptions;
    private String type;
    private boolean observable;
    private boolean readOnly;
    private boolean writeOnly;
    private Map<String, Map> uriVariables;
    private Map<String, Object> optionalProperties;
    private ExposedThingProperty exposedProperty;
    private Supplier<CompletableFuture<Object>> readHandler;
    private Subject subject;
    private Function<Object, CompletableFuture<Object>> writeHandler;

    @Before
    public void setUp() {
        thing = mock(ExposedThing.class);
        name = "foo";
        property = mock(ThingProperty.class);
        state = mock(PropertyState.class);
        readHandler = mock(Supplier.class);
        writeHandler = mock(Function.class);
        subject = mock(Subject.class);
    }

    @Test
    public void readWithoutHandlerShouldReturnStateValue() {
        exposedProperty = new ExposedThingProperty(name, thing, state, objectType, description, descriptions, type, observable, readOnly, writeOnly, uriVariables, optionalProperties);

        exposedProperty.read();

        verify(state, times(1)).getValue();
    }

    @Test
    public void readWithHandlerShouldCallHandler() {
        when(state.getReadHandler()).thenReturn(readHandler);

        exposedProperty = new ExposedThingProperty(name, thing, state, objectType, description, descriptions, type, observable, readOnly, writeOnly, uriVariables, optionalProperties);

        exposedProperty.read();

        verify(readHandler, times(1)).get();
    }

    @Test(expected = ExecutionException.class)
    public void readWithBrokenHandlerShouldReturnFailedFuture() throws ExecutionException, InterruptedException {
        when(readHandler.get()).thenThrow(new RuntimeException());
        when(state.getReadHandler()).thenReturn(readHandler);

        exposedProperty = new ExposedThingProperty(name, thing, state, objectType, description, descriptions, type, observable, readOnly, writeOnly, uriVariables, optionalProperties);

        exposedProperty.read().get();
    }

    @Test
    public void writeWithoutHandlerShouldSetStateValueAndInformSubject() {
        when(state.getSubject()).thenReturn(subject);

        exposedProperty = new ExposedThingProperty(name, thing, state, objectType, description, descriptions, type, observable, readOnly, writeOnly, uriVariables, optionalProperties);

        exposedProperty.write(1337);

        verify(state, times(1)).setValue(1337);
        verify(subject, times(1)).next(1337);
    }

    @Test
    public void writeWithHandlerShouldCallHandlerAndInformSubject() {
        when(writeHandler.apply(any())).thenReturn(completedFuture(1337));
        when(state.getWriteHandler()).thenReturn(writeHandler);
        when(state.getSubject()).thenReturn(subject);

        exposedProperty = new ExposedThingProperty(name, thing, state, objectType, description, descriptions, type, observable, readOnly, writeOnly, uriVariables, optionalProperties);

        exposedProperty.write(1337);

        verify(writeHandler, times(1)).apply(1337);
        verify(subject, times(1)).next(1337);
    }
}