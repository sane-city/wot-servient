package city.sane.wot.thing.action;

import city.sane.wot.thing.schema.DataSchema;
import city.sane.wot.thing.schema.StringSchema;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;

@ExtendWith(MockitoExtension.class)
public class ThingActionTest {
    private DataSchema input;
    private DataSchema output;

    @BeforeEach
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