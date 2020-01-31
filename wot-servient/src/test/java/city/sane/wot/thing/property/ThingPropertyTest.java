package city.sane.wot.thing.property;

import org.junit.Test;

import java.util.Map;

import static junit.framework.TestCase.assertTrue;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

public class ThingPropertyTest {
    @Test
    public void testEquals() {
        ThingProperty property1 = new ThingProperty();
        ThingProperty property2 = new ThingProperty();

        assertEquals(property1, property2);
    }

    @Test
    public void builder() {
        ThingProperty property = new ThingProperty.Builder()
                .setObjectType("saref:Temperature")
                .setType("integer")
                .setObservable(true)
                .setReadOnly(true)
                .setWriteOnly(false)
                .setOptionalProperties(Map.of("foo", "bar"))
                .build();

        assertEquals("saref:Temperature", property.getObjectType());
        assertEquals("integer", property.getType());
        assertTrue(property.isObservable());
        assertTrue(property.isReadOnly());
        assertFalse(property.isWriteOnly());
        assertEquals(Map.of("foo", "bar"), property.getOptionalProperties());
        assertEquals("bar", property.getOptional("foo"));
    }
}