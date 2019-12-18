package city.sane.wot.binding.websocket.message;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

public class AbstractServerMessageTest {
    private AbstractServerMessage am1;

    @Before
    public void setUp() throws Exception {
        am1 = new AbstractServerMessage("1234");
    }

    @After
    public void tearDown() throws Exception {
    }


    @Test
    public void getClientId() {
        assertEquals("1234",am1.getId());
        assertNotNull(am1.getId());

    }
}