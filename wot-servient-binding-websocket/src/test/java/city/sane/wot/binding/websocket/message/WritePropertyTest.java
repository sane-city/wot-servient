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
    private WriteProperty wp2;
    private WriteProperty wp3;
    private WriteProperty wp4;
    private WriteProperty wp5;
    private Content c;


    @Before
    public void setUp() throws Exception {
        c = ContentManager.valueToContent(24);
        wp = new WriteProperty("123456","test", c);
    }

    @Test(expected = NullPointerException.class)
    public void testConstructorNullParams() {
        wp2 = new WriteProperty(null, null,null);
        wp3 = new WriteProperty("123456", null, c);
        wp4 = new WriteProperty(null, "test", c);
        wp5 = new WriteProperty("123456","test",null);
    }

    @After
    public void tearDown() throws Exception {
    }

    @Test
    public void getThingId() {

        assertEquals("123456",wp.getThingId());
        assertNotNull(wp.getThingId());
    }

    @Test
    public void getName() {

        assertEquals("test",wp.getName());
        assertNotNull(wp.getName());
    }

    @Test
    public void getPayload() throws ContentCodecException {
        assertEquals(c,wp.getPayload());
        assertNotNull(wp.getPayload());
    }
}