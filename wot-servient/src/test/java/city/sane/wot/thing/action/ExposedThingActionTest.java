package city.sane.wot.thing.action;

import city.sane.wot.thing.ExposedThing;
import city.sane.wot.thing.schema.DataSchema;
import org.junit.Before;
import org.junit.Test;

import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.function.BiFunction;

import static org.junit.Assert.assertNotNull;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class ExposedThingActionTest {
    private ExposedThing thing;
    private ActionState state;
    private String description;
    private DataSchema input;
    private DataSchema output;
    private BiFunction handler;

    @Before
    public void setUp() {
        thing = mock(ExposedThing.class);
        state = mock(ActionState.class);
        description = "";
        input = mock(DataSchema.class);
        output = mock(DataSchema.class);
        handler = mock(BiFunction.class);
    }

    @Test
    public void invokeEnsureNonNullReturnValue() {
        when(state.getHandler()).thenReturn(handler);
        when(handler.apply(any(), any())).thenReturn(null);

        ExposedThingAction exposedThingAction = new ExposedThingAction("myAction", thing, state, description, Map.of(), Map.of(), input, output);

        assertNotNull(exposedThingAction.invoke());
    }
}