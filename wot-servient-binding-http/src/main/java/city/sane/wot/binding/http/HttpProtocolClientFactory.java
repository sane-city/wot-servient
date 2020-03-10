package city.sane.wot.binding.http;

import city.sane.wot.binding.ProtocolClientFactory;

/**
 * Creates new {@link HttpProtocolClient} instances.
 */
public class HttpProtocolClientFactory implements ProtocolClientFactory {
    @Override
    public String toString() {
        return "HttpClient";
    }

    @Override
    public String getScheme() {
        return "http";
    }

    @Override
    public HttpProtocolClient getClient() {
        return new HttpProtocolClient();
    }
}
