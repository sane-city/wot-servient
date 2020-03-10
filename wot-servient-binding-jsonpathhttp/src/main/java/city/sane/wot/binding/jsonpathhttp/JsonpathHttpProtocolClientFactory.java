package city.sane.wot.binding.jsonpathhttp;

import city.sane.wot.binding.ProtocolClientFactory;

/**
 * Creates new {@link JsonpathHttpProtocolClient} instances.
 */
public class JsonpathHttpProtocolClientFactory implements ProtocolClientFactory {
    @Override
    public String getScheme() {
        return "jsonpath+http";
    }

    @Override
    public JsonpathHttpProtocolClient getClient() {
        return new JsonpathHttpProtocolClient();
    }
}
