package city.sane.wot.binding.websocket;

import org.junit.Test;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

public class WebsocketProtocolClientFactoryTest {
    @Test
    public void getScheme() {
        assertEquals("wss", new WebsocketProtocolClientFactory().getScheme());
    }

    @Test
    public void getClient() {
        assertThat(new WebsocketProtocolClientFactory().getClient(), instanceOf(WebsocketProtocolClient.class));
    }
}