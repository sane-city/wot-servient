package city.sane.wot.content;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class ContentTest {
    @Test
    public void testEquals() {
        Content contentA = new Content("application/json", "Hallo Welt".getBytes());
        Content contentB = new Content("application/json", "Hallo Welt".getBytes());

        assertEquals(contentA, contentB);
    }
}