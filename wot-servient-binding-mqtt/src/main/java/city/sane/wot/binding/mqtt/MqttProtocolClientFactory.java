package city.sane.wot.binding.mqtt;

import city.sane.wot.binding.ProtocolClient;
import city.sane.wot.binding.ProtocolClientException;
import city.sane.wot.binding.ProtocolClientFactory;
import com.typesafe.config.Config;

/**
 * Creates new {@link MqttProtocolClient} instances.
 */
public class MqttProtocolClientFactory implements ProtocolClientFactory {
    private final Config config;

    public MqttProtocolClientFactory(Config config) {
        this.config = config;
    }

    @Override
    public String getScheme() {
        return "mqtt";
    }

    @Override
    public ProtocolClient getClient() throws ProtocolClientException {
        return new MqttProtocolClient(config);
    }
}
