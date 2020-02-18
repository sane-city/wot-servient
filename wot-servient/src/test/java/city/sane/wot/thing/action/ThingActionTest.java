package city.sane.wot.thing.action;

import city.sane.wot.thing.schema.DataSchema;
import city.sane.wot.thing.schema.StringSchema;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;

public class ThingActionTest {
    private DataSchema input;
    private DataSchema output;

    @Before
    public void setUp() {
        input = mock(DataSchema.class);
        output = mock(DataSchema.class);
    }

    @Test
    public void builder() {
        ThingAction<Object, Object> action = new ThingAction.Builder()
                .setInput(input)
                .setOutput(output)
                .build();

        assertEquals(input, action.getInput());
        assertEquals(output, action.getOutput());
    }

    @Test
    public void builderShouldUseStringAsDefaultInputSchema() {
        ThingAction<Object, Object> action = new ThingAction.Builder().build();

        assertEquals(new StringSchema(), action.getInput());
    }

    @Test
    public void builderShouldUseStringAsDefaultOutputSchema() {
        ThingAction<Object, Object> action = new ThingAction.Builder().build();

        assertEquals(new StringSchema(), action.getOutput());
    }
}