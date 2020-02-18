package city.sane.wot.thing.schema;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class NumberSchemaTest {
    @Test
    public void getType() {
        assertEquals("number", new NumberSchema().getType());
    }

    @Test
    public void getClassType() {
        assertEquals(Number.class, new NumberSchema().getClassType());
    }
}