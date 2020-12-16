package city.sane.wot.binding.websocket.message;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class ReadPropertyTest {
    private ReadProperty message;

    @BeforeEach
    public void setUp() {
        message = new ReadProperty("counter", "count");
    }

    @Test
    public void testConstructorNullParams() {
        assertThrows(NullPointerException.class, () -> new ReadProperty(null, null));
        assertThrows(NullPointerException.class, () -> new ReadProperty("counter", null));
        assertThrows(NullPointerException.class, () -> new ReadProperty(null, "count"));
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