package city.sane.wot.binding.websocket.message;

import city.sane.wot.content.Content;
import city.sane.wot.content.ContentCodecException;
import city.sane.wot.content.ContentManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class WritePropertyTest {
    private WriteProperty message;

    @BeforeEach
    public void setUp() throws Exception {
        Content content = ContentManager.valueToContent(24);
        message = new WriteProperty("counter", "count", content);
    }

    @Test
    public void testConstructorNullParams() {
        assertThrows(NullPointerException.class, () -> new WriteProperty(null, null, null));
        assertThrows(NullPointerException.class, () -> new WriteProperty("counter", null, ContentManager.valueToContent(24)));
        assertThrows(NullPointerException.class, () -> new WriteProperty(null, "count", ContentManager.valueToContent(24)));
        assertThrows(NullPointerException.class, () -> new WriteProperty("counter", "count", null));
    }

    @Test
    public void getThingId() {
        assertEquals("counter", message.getThingId());
        assertNotNull(message.getThingId());
    }

    @Test
    public void getName() {
        assertEquals("count", message.getName());
        assertNotNull(message.getName());
    }

    @Test
    public void getPayload() throws ContentCodecException {
        assertEquals(ContentManager.valueToContent(24), message.getValue());
    }
}