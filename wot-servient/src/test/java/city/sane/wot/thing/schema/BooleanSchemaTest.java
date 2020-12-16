package city.sane.wot.thing.schema;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class BooleanSchemaTest {
    @Test
    public void getType() {
        assertEquals("boolean", new BooleanSchema().getType());
    }

    @Test
    public void getClassType() {
        assertEquals(Boolean.class, new BooleanSchema().getClassType());
    }
}