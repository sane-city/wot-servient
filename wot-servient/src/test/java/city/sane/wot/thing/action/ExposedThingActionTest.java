package city.sane.wot.thing.action;

import city.sane.wot.thing.ExposedThing;
import city.sane.wot.thing.schema.DataSchema;
import com.github.jsonldjava.utils.Obj;
import org.junit.Before;
import org.junit.Test;

import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.function.BiFunction;

import static org.junit.Assert.assertNotNull;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;

public class ExposedThingActionTest {
    private ExposedThing thing;
    private ActionState state;
    private String description;
    private DataSchema input;
    private DataSchema output;
    private BiFunction handler;
    private Object invokeInput;
    private Map<String, Object> invokeOptions;

    @Before
    public void setUp() {
        thing = mock(ExposedThing.class);
        state = mock(ActionState.class);
        description = "";
        input = mock(DataSchema.class);
        output = mock(DataSchema.class);
        handler = mock(BiFunction.class);
        invokeInput = mock(Object.class);
        invokeOptions = mock(Map.class);
    }

    @Test
    public void invokeWithoutHandlerShouldReturnNullFuture() {
        when(state.getHandler()).thenReturn(handler);

        ExposedThingAction exposedThingAction = new ExposedThingAction("myAction", thing, state, description, Map.of(), Map.of(), input, output);
        CompletableFuture<Object> result = exposedThingAction.invoke(invokeInput, invokeOptions);

        assertNotNull(result);
    }

    @Test
    public void invokeWithHandlerShouldCallHandler() {
        when(state.getHandler()).thenReturn(handler);

        ExposedThingAction exposedThingAction = new ExposedThingAction("myAction", thing, state, description, Map.of(), Map.of(), input, output);
        exposedThingAction.invoke(invokeInput, invokeOptions);

        verify(handler, times(1)).apply(invokeInput, invokeOptions);
    }

    @Test(expected = ExecutionException.class)
    public void invokeWithBrokenHandlerShouldReturnFailedFuture() throws ExecutionException, InterruptedException {
        when(handler.apply(any(), any())).thenThrow(new RuntimeException());
        when(state.getHandler()).thenReturn(handler);

        ExposedThingAction exposedThingAction = new ExposedThingAction("myAction", thing, state, description, Map.of(), Map.of(), input, output);
        exposedThingAction.invoke(invokeInput, invokeOptions).get();

        verify(handler, times(1)).apply(invokeInput, invokeOptions);
    }

    @Test
    public void invokeShouldWrapNullResultsFromHandlerIntoFuture() {
        when(state.getHandler()).thenReturn(handler);
        when(handler.apply(any(), any())).thenReturn(null);

        ExposedThingAction exposedThingAction = new ExposedThingAction("myAction", thing, state, description, Map.of(), Map.of(), input, output);

        assertNotNull(exposedThingAction.invoke());
    }
}