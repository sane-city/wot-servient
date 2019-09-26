package city.sane.wot.binding.http;

import city.sane.wot.binding.ProtocolClient;
import city.sane.wot.binding.ProtocolClientFactory;
import com.typesafe.config.Config;

/**
 * Creates new {@link HttpProtocolClient} instances.
 */
public class HttpProtocolClientFactory implements ProtocolClientFactory {
    public HttpProtocolClientFactory(Config config) {
    }

    @Override
    public String toString() {
        return "HttpClient";
    }

    @Override
    public String getScheme() {
        return "http";
    }

    @Override
    public ProtocolClient getClient() {
        return new HttpProtocolClient();
    }
}
