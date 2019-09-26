package city.sane.wot.binding.value;

import city.sane.wot.binding.ProtocolClient;
import city.sane.wot.binding.ProtocolClientFactory;
import com.typesafe.config.Config;

/**
 * Creates new {@link ValueProtocolClient} instances.
 */
public class ValueProtocolClientFactory implements ProtocolClientFactory {
    public ValueProtocolClientFactory(Config config) {

    }

    @Override
    public String getScheme() {
        return "value";
    }

    @Override
    public ProtocolClient getClient() {
        return new ValueProtocolClient();
    }
}
