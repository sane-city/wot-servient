package city.sane.wot.binding.websocket.message;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

public class SubscribePropertyTest {
    SubscribeProperty sp;

    @Before
    public void setUp() throws Exception {
        sp = new SubscribeProperty("123456","test");
    }

    @After
    public void tearDown() throws Exception {
    }

    @Test
    public void getThingId() {
        assertEquals("123456",sp.getThingId());
        assertNotNull(sp.getThingId());
    }

    @Test
    public void getName() {
        assertEquals("test",sp.getName());
        assertNotNull(sp.getName());
    }
}