package city.sane.wot.binding.coap.resource;

import city.sane.wot.content.Content;
import city.sane.wot.content.ContentCodecException;
import city.sane.wot.content.ContentManager;
import city.sane.wot.thing.action.ExposedThingAction;
import org.eclipse.californium.core.coap.CoAP;
import org.eclipse.californium.core.coap.MediaTypeRegistry;
import org.eclipse.californium.core.server.resources.CoapExchange;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Endpoint for interaction with a {@link city.sane.wot.thing.action.ThingAction}.
 */
public class ActionResource extends AbstractResource {
    private static final Logger log = LoggerFactory.getLogger(ActionResource.class);
    private final ExposedThingAction<Object, Object> action;

    public ActionResource(String name, ExposedThingAction<Object, Object> action) {
        super(name);
        this.action = action;
    }

    @Override
    public void handlePOST(CoapExchange exchange) {
        log.debug("Handle POST to '{}'", getURI());

        String requestContentFormat = getOrDefaultRequestContentType(exchange);
        if (!ContentManager.isSupportedMediaType(requestContentFormat)) {
            log.warn("Unsupported media type: {}", requestContentFormat);
            String payload = "Unsupported Media Type (supported: " + String.join(", ", ContentManager.getSupportedMediaTypes()) + ")";
            exchange.respond(CoAP.ResponseCode.UNSUPPORTED_CONTENT_FORMAT, payload);
        }

        byte[] requestPayload = exchange.getRequestPayload();

        Content inputContent = new Content(requestContentFormat, requestPayload);
        try {
            Object input = ContentManager.contentToValue(inputContent, action.getInput());

            action.invoke(input).whenComplete((value, e) -> {
                if (e == null) {
                    try {
                        Content content = ContentManager.valueToContent(value, requestContentFormat);

                        int contentFormat = MediaTypeRegistry.parse(content.getType());
                        exchange.respond(CoAP.ResponseCode.CONTENT, content.getBody(), contentFormat);
                    }
                    catch (ContentCodecException ex) {
                        exchange.respond(CoAP.ResponseCode.SERVICE_UNAVAILABLE, ex.toString());
                    }
                }
                else {
                    exchange.respond(CoAP.ResponseCode.SERVICE_UNAVAILABLE, e.toString());
                }
            });
        }
        catch (ContentCodecException e) {
            exchange.respond(CoAP.ResponseCode.SERVICE_UNAVAILABLE, e.toString());
        }
    }
}
