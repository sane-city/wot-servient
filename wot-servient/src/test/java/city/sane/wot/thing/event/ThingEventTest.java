package city.sane.wot.thing.event;

import city.sane.wot.thing.schema.DataSchema;
import city.sane.wot.thing.schema.StringSchema;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;

public class ThingEventTest {
    private DataSchema data;

    @BeforeEach
    public void setUp() {
        data = mock(DataSchema.class);
    }

    @Test
    public void builder() {
        ThingEvent<Object> event = new ThingEvent.Builder()
                .setData(data)
                .setType("integer")
                .build();

        assertEquals(data, event.getData());
        assertEquals("integer", event.getType());
    }

    @Test
    public void builderShouldUseStringAsDefaultDataSchema() {
        ThingEvent<Object> event = new ThingEvent.Builder().build();

        assertEquals(new StringSchema(), event.getData());
    }
}