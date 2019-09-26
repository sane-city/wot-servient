package city.sane.wot.binding.http;

import com.typesafe.config.Config;

/**
 * Creates new {@link HttpProtocolClient} instances that allow consuming Things via HTTPS.
 */
public class HttpsProtocolClientFactory extends HttpProtocolClientFactory {
    public HttpsProtocolClientFactory(Config config) {
        super(config);
    }

    @Override
    public String toString() {
        return "HttpsClient";
    }

    @Override
    public String getScheme() {
        return "https";
    }
}
