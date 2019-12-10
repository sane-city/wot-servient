package city.sane.wot.thing.schema;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

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