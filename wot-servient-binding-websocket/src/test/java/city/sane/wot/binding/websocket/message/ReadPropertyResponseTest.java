package city.sane.wot.binding.websocket.message;

import city.sane.wot.content.Content;
import city.sane.wot.content.ContentManager;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class ReadPropertyResponseTest {
    private ReadProperty clientMessage;
    private ReadPropertyResponse rpr1;
    private ReadPropertyResponse rpr2;
    private Content value;

    @Before
    public void setUp() throws Exception {
        clientMessage = new ReadProperty("123", "test");
        value = ContentManager.valueToContent(24);
        rpr1 = new ReadPropertyResponse(clientMessage, value);
    }

    @After
    public void tearDown() throws Exception {
    }

    @Test(expected = NullPointerException.class)
    public void testConstructorWithNull() {
        rpr2 = new ReadPropertyResponse((ReadProperty) null, null);
    }

    @Test
    public void getValue() {
        assertEquals(value, rpr1.getContent());
        assertNotNull(rpr1.getContent());
    }
}