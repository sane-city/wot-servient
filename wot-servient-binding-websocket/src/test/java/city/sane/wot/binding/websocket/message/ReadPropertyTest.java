package city.sane.wot.binding.websocket.message;

import com.typesafe.config.ConfigException;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

public class ReadPropertyTest {
    private ReadProperty rp;
    private ReadProperty rp2;
    private ReadProperty rp3;
    private ReadProperty rp4;
    private ReadProperty rp5;

    @Before
    public void setUp() throws Exception {
        rp = new ReadProperty("123456","test");
    }

    @After
    public void tearDown() throws Exception {
    }

    @Test(expected = NullPointerException.class)
    public void testConstructorNullParams() {
        rp2 = new ReadProperty(null, null);
        rp3 = new ReadProperty("123456", null);
        rp4 = new ReadProperty(null, "test");
    }


    @Test
    public void getThingId() {
        assertEquals("123456",rp.getThingId());
        assertNotNull(rp.getThingId());
    }

    @Test
    public void getName() {
        assertEquals("test",rp.getName());
        assertNotNull(rp.getName());
    }
}