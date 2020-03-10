package city.sane.wot.binding.coap;

import com.typesafe.config.ConfigFactory;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertEquals;

public class CoapProtocolClientFactoryTest {
    @Test
    public void getScheme() {
        assertEquals("coap", new CoapProtocolClientFactory(ConfigFactory.load()).getScheme());
    }

    @Test
    public void getClient() {
        assertThat(new CoapProtocolClientFactory(ConfigFactory.load()).getClient(), instanceOf(CoapProtocolClient.class));
    }
}