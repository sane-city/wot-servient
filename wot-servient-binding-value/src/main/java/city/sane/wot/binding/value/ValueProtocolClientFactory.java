package city.sane.wot.binding.value;

import city.sane.wot.binding.ProtocolClient;
import city.sane.wot.binding.ProtocolClientFactory;

/**
 * Creates new {@link ValueProtocolClient} instances.
 */
public class ValueProtocolClientFactory implements ProtocolClientFactory {
    @Override
    public String getScheme() {
        return "value";
    }

    @Override
    public ProtocolClient getClient() {
        return new ValueProtocolClient();
    }
}
