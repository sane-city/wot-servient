package city.sane.wot.content;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

public class ContentTest {
    @Test
    public void testEquals() {
        Content contentA = new Content("application/json", "Hallo Welt".getBytes());
        Content contentB = new Content("application/json", "Hallo Welt".getBytes());

        assertEquals(contentA, contentB);
    }

    @Test
    public void testEqualsSameObject() {
        Content content = new Content("application/json", "Hallo Welt".getBytes());

        assertEquals(content, content);
    }

    @Test
    public void testEqualsNull() {
        Content content = new Content("application/json", "Hallo Welt".getBytes());

        assertNotEquals(null, content);
    }

    @Test
    public void testEqualsNoContent() {
        Content content = new Content("application/json", "Hallo Welt".getBytes());

        assertNotEquals("Hallo Welt", content);
    }
}