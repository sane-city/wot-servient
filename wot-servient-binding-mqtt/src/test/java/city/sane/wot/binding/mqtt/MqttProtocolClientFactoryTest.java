package city.sane.wot.binding.mqtt;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class MqttProtocolClientFactoryTest {
    @Test
    public void getScheme() {
        assertEquals("mqtt", new MqttProtocolClientFactory(null).getScheme());
    }
}