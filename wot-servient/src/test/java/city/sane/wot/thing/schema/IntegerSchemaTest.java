package city.sane.wot.thing.schema;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class IntegerSchemaTest {
    @Test
    public void getType() {
        assertEquals("integer", new IntegerSchema().getType());
    }

    @Test
    public void getClassType() {
        assertEquals(Integer.class, new IntegerSchema().getClassType());
    }
}