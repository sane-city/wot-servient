package city.sane.wot.binding.coap;

import org.junit.Test;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertEquals;

public class CoapProtocolClientFactoryTest {
    @Test
    public void getScheme() {
        assertEquals("coap", new CoapProtocolClientFactory().getScheme());
    }

    @Test
    public void getClient() {
        assertThat(new CoapProtocolClientFactory().getClient(), instanceOf(CoapProtocolClient.class));
    }
}