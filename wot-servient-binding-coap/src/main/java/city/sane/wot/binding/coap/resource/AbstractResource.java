package city.sane.wot.binding.coap.resource;

import city.sane.wot.thing.content.ContentManager;
import org.eclipse.californium.core.CoapResource;
import org.eclipse.californium.core.coap.MediaTypeRegistry;
import org.eclipse.californium.core.server.resources.CoapExchange;

/**
 * Abstract resource for exposing Things. Inherited from all other resources.
 */
public abstract class AbstractResource extends CoapResource {
    public AbstractResource(String name) {
        super(name);
    }

    protected String getOrDefaultRequestContentType(CoapExchange exchange) {
        int requestContentFormatNum = exchange.getRequestOptions().getContentFormat();
        String requestContentFormat;
        if (requestContentFormatNum != -1) {
            return MediaTypeRegistry.toString(requestContentFormatNum);
        }
        else {
            return ContentManager.DEFAULT;
        }
    }
}
