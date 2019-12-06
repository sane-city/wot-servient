package city.sane.wot.thing.form;

import org.junit.Test;

import java.util.Collections;
import java.util.Map;

import static org.junit.Assert.assertEquals;

public class FormTest {
    @Test
    public void builder() {
        Form property = new Form.Builder()
                .setHref("test:/foo")
                .setOp(Operation.OBSERVE_PROPERTY)
                .setSubprotocol("longpolling")
                .setContentType("application/json")
                .setOptionalProperties(Map.of("foo", "bar"))
                .build();

        assertEquals("test:/foo", property.getHref());
        assertEquals(Collections.singletonList(Operation.OBSERVE_PROPERTY), property.getOp());
        assertEquals("longpolling", property.getSubprotocol());
        assertEquals("application/json", property.getContentType());
        assertEquals(Map.of("foo", "bar"), property.getOptionalProperties());
        assertEquals("bar", property.getOptional("foo"));
    }
}