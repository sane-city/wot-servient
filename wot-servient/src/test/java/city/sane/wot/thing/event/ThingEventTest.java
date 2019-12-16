package city.sane.wot.thing.event;

import city.sane.wot.thing.schema.NumberSchema;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class ThingEventTest {
    @Test
    public void builder() {
        ThingEvent event = new ThingEvent.Builder()
                .setData(new NumberSchema())
                .setType("integer")
                .build();

        assertEquals(new NumberSchema(), event.getData());
        assertEquals("integer", event.getType());
    }
}