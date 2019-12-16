package city.sane.wot.thing.schema;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class ObjectSchemaTest {
    @Test
    public void getType() {
        assertEquals("string", new StringSchema().getType());
    }

    @Test
    public void getClassType() {
        assertEquals(String.class, new StringSchema().getClassType());
    }
}