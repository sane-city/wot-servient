package city.sane.wot.binding.websocket.message;

import city.sane.wot.content.Content;
import city.sane.wot.content.ContentCodecException;
import city.sane.wot.content.ContentManager;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

public class WritePropertyTest {
    private WriteProperty message;

    @Before
    public void setUp() throws Exception {
        Content content = ContentManager.valueToContent(24);
        message = new WriteProperty("counter","count", content);
    }

    @Test(expected = NullPointerException.class)
    public void testConstructorNullParams() throws ContentCodecException {
        new WriteProperty(null, null, null);
        new WriteProperty("counter", null, ContentManager.valueToContent(24));
        new WriteProperty(null, "count", ContentManager.valueToContent(24));
        new WriteProperty("counter", "count", null);
    }

    @Test
    public void getThingId() {
        assertEquals("counter", message.getThingId());
        assertNotNull(message.getThingId());
    }

    @Test
    public void getName() {
        assertEquals("test", message.getName());
        assertNotNull(message.getName());
    }

    @Test
    public void getPayload() throws ContentCodecException {
        assertEquals(ContentManager.valueToContent(24), message.getValue());
    }
}