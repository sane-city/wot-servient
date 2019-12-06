package city.sane.wot.thing.action;

import city.sane.wot.thing.schema.NumberSchema;
import city.sane.wot.thing.schema.StringSchema;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class ThingActionTest {
    @Test
    public void builder() {
        ThingAction action = new ThingAction.Builder()
                .setInput(new NumberSchema())
                .setOutput(new StringSchema())
                .build();

        assertEquals(new NumberSchema(), action.getInput());
        assertEquals(new StringSchema(), action.getOutput());
    }
}