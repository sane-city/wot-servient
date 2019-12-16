package city.sane.wot.binding.http;

/**
 * Creates new {@link HttpProtocolClient} instances that allow consuming Things via HTTPS.
 */
public class HttpsProtocolClientFactory extends HttpProtocolClientFactory {
    public HttpsProtocolClientFactory() {
        super();
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
