package city.sane.wot.binding.coap.resource;

import city.sane.wot.binding.coap.CoapServer;
import city.sane.wot.content.Content;
import city.sane.wot.content.ContentCodecException;
import city.sane.wot.content.ContentManager;
import city.sane.wot.thing.ExposedThing;
import org.eclipse.californium.core.coap.CoAP;
import org.eclipse.californium.core.coap.MediaTypeRegistry;
import org.eclipse.californium.core.server.resources.CoapExchange;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Endpoint for displaying a Thing Description.
 */
public class ThingResource extends AbstractResource {
    final static Logger log = LoggerFactory.getLogger(ThingResource.class);

    private final CoapServer server;
    private final ExposedThing thing;

    public ThingResource(CoapServer server, ExposedThing thing) {
        super(thing.getId());
        this.server = server;
        this.thing = thing;
    }

    @Override
    public void handleGET(CoapExchange exchange) {
        log.info("Handles GET to '{}'", getURI());

        String requestContentFormat = getOrDefaultRequestContentType(exchange);
        if (!ContentManager.isSupportedMediaType(requestContentFormat)) {
            log.warn("Unsupported media type: {}", requestContentFormat);
            String payload = "Unsupported Media Type (supported: " + String.join(", ", ContentManager.getSupportedMediaTypes()) + ")";
            exchange.respond(CoAP.ResponseCode.UNSUPPORTED_CONTENT_FORMAT, payload);
        }

        try {
            Content content = ContentManager.valueToContent(thing, requestContentFormat);
            int contentFormat = MediaTypeRegistry.parse(content.getType());

            exchange.respond(CoAP.ResponseCode.CONTENT, content.getBody(), contentFormat);
        }
        catch (ContentCodecException e) {
            log.warn("Exception: {}", e);
            exchange.respond(CoAP.ResponseCode.SERVICE_UNAVAILABLE, e.toString());
        }
    }
}
