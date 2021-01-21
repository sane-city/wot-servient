package city.sane.wot.thing.schema;

import org.junit.jupiter.api.Test;

import static org.junit.Assert.assertEquals;

public class NullSchemaTest {
    @Test
    public void getType() {
        assertEquals("null", new NullSchema().getType());
    }

    @Test
    public void getClassType() {
        assertEquals(Object.class, new NullSchema().getClassType());
    }
}