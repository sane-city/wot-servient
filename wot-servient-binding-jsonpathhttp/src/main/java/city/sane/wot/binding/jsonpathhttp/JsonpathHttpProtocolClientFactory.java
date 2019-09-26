package city.sane.wot.binding.jsonpathhttp;

import city.sane.wot.binding.ProtocolClient;
import city.sane.wot.binding.ProtocolClientFactory;
import com.typesafe.config.Config;

/**
 * Creates new {@link JsonpathHttpProtocolClient} instances.
 */
public class JsonpathHttpProtocolClientFactory implements ProtocolClientFactory {
    public JsonpathHttpProtocolClientFactory(Config config) {
    }

    @Override
    public String getScheme() {
        return "jsonpath+http";
    }

    @Override
    public ProtocolClient getClient() {
        return new JsonpathHttpProtocolClient();
    }
}
