package city.sane.wot.binding.value;

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
    public ValueProtocolClient getClient() {
        return new ValueProtocolClient();
    }
}
