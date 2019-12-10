package city.sane.wot.binding.mqtt;

import city.sane.wot.binding.ProtocolClientException;
import com.typesafe.config.ConfigFactory;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertEquals;

public class MqttProtocolClientFactoryTest {
    @Test
    public void getScheme() {
        assertEquals("mqtt", new MqttProtocolClientFactory(ConfigFactory.load()).getScheme());
    }

    @Test
    public void getClient() throws ProtocolClientException {
        assertThat(new MqttProtocolClientFactory(ConfigFactory.load()).getClient(), instanceOf(MqttProtocolClient.class));
    }
}