package city.sane.wot.thing.action;

import city.sane.wot.thing.ExposedThing;
import city.sane.wot.thing.schema.DataSchema;
import org.junit.Before;
import org.junit.Test;

import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.function.BiFunction;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class ExposedThingActionTest {
    private ExposedThing thing;
    private ActionState<Object, Object> state;
    private String description;
    private DataSchema input;
    private DataSchema output;
    private BiFunction handler;
    private Object invokeInput;
    private Map<String, Map<String, Object>> invokeOptions;

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
    public void invokeWithoutHandlerShouldReturnNullFuture() throws ExecutionException, InterruptedException {
        ExposedThingAction<Object, Object> exposedThingAction = new ExposedThingAction<Object, Object>("myAction", thing, state, description, Map.of(), Map.of(), input, output);
        exposedThingAction.invoke(invokeInput, invokeOptions);

        assertNull(exposedThingAction.invoke(invokeInput, invokeOptions).get());
    }

    @Test
    public void invokeWithHandlerShouldCallHandler() {
        when(state.getHandler()).thenReturn(handler);

        ExposedThingAction<Object, Object> exposedThingAction = new ExposedThingAction<Object, Object>("myAction", thing, state, description, Map.of(), Map.of(), input, output);
        exposedThingAction.invoke(invokeInput, invokeOptions);

        verify(handler).apply(invokeInput, invokeOptions);
    }

    @Test(expected = ExecutionException.class)
    public void invokeWithBrokenHandlerShouldReturnFailedFuture() throws ExecutionException, InterruptedException {
        when(handler.apply(any(), any())).thenThrow(new RuntimeException());
        when(state.getHandler()).thenReturn(handler);

        ExposedThingAction<Object, Object> exposedThingAction = new ExposedThingAction<Object, Object>("myAction", thing, state, description, Map.of(), Map.of(), input, output);
        exposedThingAction.invoke(invokeInput, invokeOptions).get();

        verify(handler).apply(invokeInput, invokeOptions);
    }

    @Test
    public void invokeShouldWrapNullResultsFromHandlerIntoFuture() {
        when(state.getHandler()).thenReturn(handler);
        when(handler.apply(any(), any())).thenReturn(null);

        ExposedThingAction<Object, Object> exposedThingAction = new ExposedThingAction<Object, Object>("myAction", thing, state, description, Map.of(), Map.of(), input, output);

        assertNotNull(exposedThingAction.invoke());
    }
}