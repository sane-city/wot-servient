package city.sane.wot.binding.websocket.message;

import city.sane.wot.content.Content;
import city.sane.wot.content.ContentManager;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class ReadPropertyResponseTest {
    private ReadProperty clientMessage;
    private ReadPropertyResponse rpr1;
    private ReadPropertyResponse rpr2;
    private Content value;

    @BeforeEach
    public void setUp() throws Exception {
        clientMessage = new ReadProperty("123", "test");
        value = ContentManager.valueToContent(24);
        rpr1 = new ReadPropertyResponse(clientMessage, value);
    }

    @AfterEach
    public void tearDown() throws Exception {
    }

    @Test
    public void testConstructorWithNull() {
        assertThrows(NullPointerException.class, () -> new ReadPropertyResponse((ReadProperty) null, null));
    }

    @Test
    public void getValue() {
        assertEquals(value, rpr1.getContent());
        assertNotNull(rpr1.getContent());
    }
}