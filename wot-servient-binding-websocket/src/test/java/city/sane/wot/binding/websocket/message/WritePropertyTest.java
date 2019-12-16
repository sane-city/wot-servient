package city.sane.wot.binding.websocket.message;

import city.sane.wot.content.Content;
import city.sane.wot.content.ContentCodecException;
import city.sane.wot.content.ContentManager;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

public class WritePropertyTest {
    private WriteProperty wp;
    private Content c;
    @Before
    public void setUp() throws Exception {
        c = ContentManager.valueToContent(24);
        wp = new WriteProperty("123456","test", c);
    }

    @After
    public void tearDown() throws Exception {
    }

    @Test
    public void getThingId() {
        assertEquals("123456",wp.getThingId());
    }

    @Test
    public void getName() {
        assertEquals("test",wp.getName());
    }

    @Test
    public void getPayload() throws ContentCodecException {
        assertEquals(c,wp.getPayload());
    }
}