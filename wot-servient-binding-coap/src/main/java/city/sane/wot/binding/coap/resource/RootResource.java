package city.sane.wot.binding.coap.resource;

import city.sane.wot.binding.coap.CoapServer;
import city.sane.wot.content.Content;
import city.sane.wot.content.ContentCodecException;
import city.sane.wot.content.ContentManager;
import org.eclipse.californium.core.coap.CoAP;
import org.eclipse.californium.core.coap.MediaTypeRegistry;
import org.eclipse.californium.core.server.resources.CoapExchange;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Endpoint for listing all Things from the {@link city.sane.wot.Servient}.
 */
public class RootResource extends AbstractResource {
    final static Logger log = LoggerFactory.getLogger(RootResource.class);

    private final CoapServer server;

    public RootResource(CoapServer server) {
        super("");
        this.server = server;
    }

    @Override
    public void handleGET(CoapExchange exchange) {
        log.info("Handle GET to '{}'", getURI());

        String requestContentFormat = getOrDefaultRequestContentType(exchange);
        if (!ContentManager.isSupportedMediaType(requestContentFormat)) {
            log.warn("Unsupported media type: {}", requestContentFormat);
            String payload = "Unsupported Media Type (supported: " + String.join(", ", ContentManager.getSupportedMediaTypes()) + ")";
            exchange.respond(CoAP.ResponseCode.UNSUPPORTED_CONTENT_FORMAT, payload);
        }

        try {
            Content content = ContentManager.valueToContent(server.getProtocolServer().getThings(), requestContentFormat);
            int contentFormat = MediaTypeRegistry.parse(content.getType());

            exchange.respond(CoAP.ResponseCode.CONTENT, content.getBody(), contentFormat);
        }
        catch (ContentCodecException e) {
            e.printStackTrace();
            exchange.respond(CoAP.ResponseCode.SERVICE_UNAVAILABLE, e.toString());
        }
    }
}
