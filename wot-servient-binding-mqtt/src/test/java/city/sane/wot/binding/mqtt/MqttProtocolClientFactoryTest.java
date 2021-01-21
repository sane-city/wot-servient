package city.sane.wot.binding.mqtt;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class MqttProtocolClientFactoryTest {
    @Test
    public void getScheme() {
        assertEquals("mqtt", new MqttProtocolClientFactory(null).getScheme());
    }
}