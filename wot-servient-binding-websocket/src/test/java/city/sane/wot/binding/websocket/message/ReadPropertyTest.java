package city.sane.wot.binding.websocket.message;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

public class ReadPropertyTest {
    private ReadProperty message;

    @Before
    public void setUp() {
        message = new ReadProperty("counter","count");
    }

    @Test(expected = NullPointerException.class)
    public void testConstructorNullParams() {
        new ReadProperty(null, null);
        new ReadProperty("counter", null);
        new ReadProperty(null, "count");
    }

    @Test
    public void getThingId() {
        assertEquals("counter", message.getThingId());
    }

    @Test
    public void getName() {
        assertEquals("count", message.getName());
    }
}