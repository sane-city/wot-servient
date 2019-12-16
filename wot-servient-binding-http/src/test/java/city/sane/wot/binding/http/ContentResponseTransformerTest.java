package city.sane.wot.binding.http;

import city.sane.wot.content.Content;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class ContentResponseTransformerTest {
    @Test
    public void renderContent() {
        Content content = new Content("application/json", "Hallo Welt".getBytes());

        assertEquals("Hallo Welt", new ContentResponseTransformer().render(content));
    }

    @Test
    public void renderObject() {
        assertEquals("1337", new ContentResponseTransformer().render(1337));
    }

    @Test
    public void renderNull() {
        assertNull(new ContentResponseTransformer().render(null));
    }
}