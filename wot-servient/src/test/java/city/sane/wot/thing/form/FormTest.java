package city.sane.wot.thing.form;

import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.Map;

import static org.junit.Assert.assertEquals;

public class FormTest {
    @Test
    public void builder() {
        Form form = new Form.Builder()
                .setHref("test:/foo")
                .setOp(Operation.OBSERVE_PROPERTY)
                .setSubprotocol("longpolling")
                .setContentType("application/json")
                .setOptionalProperties(Map.of("foo", "bar"))
                .build();

        assertEquals("test:/foo", form.getHref());
        assertEquals(Collections.singletonList(Operation.OBSERVE_PROPERTY), form.getOp());
        assertEquals("longpolling", form.getSubprotocol());
        assertEquals("application/json", form.getContentType());
        assertEquals(Map.of("foo", "bar"), form.getOptionalProperties());
        assertEquals("bar", form.getOptional("foo"));
    }
}