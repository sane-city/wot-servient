package city.sane.wot.binding.value;

import city.sane.wot.content.Content;
import city.sane.wot.thing.form.Form;
import org.junit.Before;
import org.junit.Test;

import java.util.concurrent.ExecutionException;

import static org.junit.Assert.assertEquals;

public class ValueProtocolClientTest {
    private ValueProtocolClient client;

    @Before
    public void setUp() {
        client = new ValueProtocolClient();
    }

    @Test
    public void readResource() throws ExecutionException, InterruptedException {
        Content content = client.readResource(new Form.Builder()
                .setHref("value:/Hallo Welt")
                .build()
        ).get();

        assertEquals("Hallo Welt", new String(content.getBody()));
    }
}