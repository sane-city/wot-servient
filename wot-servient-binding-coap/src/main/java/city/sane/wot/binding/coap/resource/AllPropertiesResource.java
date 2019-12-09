package city.sane.wot.binding.coap.resource;

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
 * Endpoint for reading all properties from a Thing
 */
public class AllPropertiesResource extends AbstractResource {
    static final Logger log = LoggerFactory.getLogger(AllPropertiesResource.class);
    private final ExposedThing thing;

    public AllPropertiesResource(ExposedThing thing) {
        super("properties");
        this.thing = thing;
    }

    @Override
    public void handleGET(CoapExchange exchange) {
        log.info("Handle GET to '{}'", getURI());

        String requestContentFormat = getOrDefaultRequestContentType(exchange);

        if (ensureSupportedContentFormat(exchange, requestContentFormat)) {
            thing.readProperties().whenComplete((values, e) -> {
                if (e == null) {
                    // remove writeOnly properties
                    values.entrySet().removeIf(entry -> thing.getProperty(entry.getKey()).isWriteOnly());

                    try {
                        Content content = ContentManager.valueToContent(values, requestContentFormat);

                        int contentFormat = MediaTypeRegistry.parse(content.getType());
                        exchange.respond(CoAP.ResponseCode.CONTENT, content.getBody(), contentFormat);
                    }
                    catch (ContentCodecException ex) {
                        log.warn("Exception: {}", ex);
                        exchange.respond(CoAP.ResponseCode.SERVICE_UNAVAILABLE, ex.toString());
                    }
                }
                else {
                    log.warn("Exception: {}", e);
                    exchange.respond(CoAP.ResponseCode.SERVICE_UNAVAILABLE, e.toString());
                }
            });
        }
    }
}
