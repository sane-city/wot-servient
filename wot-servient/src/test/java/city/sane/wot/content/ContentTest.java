package city.sane.wot.content;

import org.junit.Test;

import static org.junit.Assert.assertTrue;

public class ContentTest {
    @Test
    public void testEquals() {
        Content contentA = new Content("application/json", "Hallo Welt".getBytes());
        Content contentB = new Content("application/json", "Hallo Welt".getBytes());

        assertTrue(contentA.equals(contentB));
    }
}