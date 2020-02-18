package city.sane.wot.thing.event;

import city.sane.wot.thing.schema.DataSchema;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;

public class ThingEventTest {
    private DataSchema data;

    @Before
    public void setUp() {
        data = mock(DataSchema.class);
    }

    @Test
    public void builder() {
        ThingEvent event = new ThingEvent.Builder()
                .setData(data)
                .setType("integer")
                .build();

        assertEquals(data, event.getData());
        assertEquals("integer", event.getType());
    }
}