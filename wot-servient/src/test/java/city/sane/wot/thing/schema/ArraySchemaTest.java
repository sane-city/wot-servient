package city.sane.wot.thing.schema;

import org.junit.Test;

import java.util.List;

import static org.junit.Assert.assertEquals;

public class ArraySchemaTest {
    @Test
    public void getType() {
        assertEquals("array", new ArraySchema().getType());
    }

    @Test
    public void getClassType() {
        assertEquals(List.class, new ArraySchema().getClassType());
    }
}