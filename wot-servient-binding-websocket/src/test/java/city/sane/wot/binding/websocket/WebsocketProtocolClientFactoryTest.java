package city.sane.wot.binding.websocket;

import org.junit.jupiter.api.Test;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class WebsocketProtocolClientFactoryTest {
    @Test
    public void getScheme() {
        assertEquals("ws", new WebsocketProtocolClientFactory().getScheme());
    }

    @Test
    public void getClient() {
        assertThat(new WebsocketProtocolClientFactory().getClient(), instanceOf(WebsocketProtocolClient.class));
    }
}