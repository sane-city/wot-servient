package city.sane.wot.binding.coap.resource;

import city.sane.wot.content.ContentManager;
import org.eclipse.californium.core.CoapResource;
import org.eclipse.californium.core.coap.CoAP;
import org.eclipse.californium.core.coap.MediaTypeRegistry;
import org.eclipse.californium.core.server.resources.CoapExchange;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Abstract resource for exposing Things. Inherited from all other resources.
 */
public abstract class AbstractResource extends CoapResource {
    static final Logger log = LoggerFactory.getLogger(AbstractResource.class);

    public AbstractResource(String name) {
        super(name);
    }

    protected String getOrDefaultRequestContentType(CoapExchange exchange) {
        int requestContentFormatNum = exchange.getRequestOptions().getContentFormat();
        if (requestContentFormatNum != -1) {
            return MediaTypeRegistry.toString(requestContentFormatNum);
        }
        else {
            return ContentManager.DEFAULT;
        }
    }

    protected boolean ensureSupportedContentFormat(CoapExchange exchange, String requestContentFormat) {
        if (ContentManager.isSupportedMediaType(requestContentFormat)) {
            return true;
        }
        else {
            log.warn("Unsupported media type: {}", requestContentFormat);
            String payload = "Unsupported Media Type (supported: " + String.join(", ", ContentManager.getSupportedMediaTypes()) + ")";
            exchange.respond(CoAP.ResponseCode.UNSUPPORTED_CONTENT_FORMAT, payload);
            return false;
        }
    }
}
