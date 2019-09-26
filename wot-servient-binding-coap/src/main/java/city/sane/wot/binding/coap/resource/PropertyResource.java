package city.sane.wot.binding.coap.resource;

import city.sane.wot.binding.coap.CoapServer;
import city.sane.wot.thing.content.Content;
import city.sane.wot.thing.content.ContentCodecException;
import city.sane.wot.thing.content.ContentManager;
import city.sane.wot.thing.property.ExposedThingProperty;
import org.eclipse.californium.core.coap.CoAP;
import org.eclipse.californium.core.coap.MediaTypeRegistry;
import org.eclipse.californium.core.server.resources.CoapExchange;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Endpoint for interaction with a {@link city.sane.wot.thing.property.ThingProperty}.
 */
public class PropertyResource extends AbstractResource {
    final static Logger log = LoggerFactory.getLogger(PropertyResource.class);

    private final CoapServer server;
    private final ExposedThingProperty property;

    public PropertyResource(CoapServer server, String name, ExposedThingProperty property) {
        super(name);
        this.server = server;
        this.property = property;
    }

    @Override
    public void handleGET(CoapExchange exchange) {
        log.info("Handle GET to '{}'", getURI());
        if (!property.isWriteOnly()) {
            String requestContentFormat = getOrDefaultRequestContentType(exchange);

            property.read().whenComplete((value, e) -> {
                if (e == null) {
                    try {
                        Content content = ContentManager.valueToContent(value, requestContentFormat);

                        int contentFormat = MediaTypeRegistry.parse(content.getType());
                        exchange.respond(CoAP.ResponseCode.CONTENT, content.getBody(), contentFormat);
                    }
                    catch (ContentCodecException ex) {
                        e.printStackTrace();
                        exchange.respond(CoAP.ResponseCode.SERVICE_UNAVAILABLE, e.toString());
                    }
                }
                else {
                    e.printStackTrace();
                    exchange.respond(CoAP.ResponseCode.SERVICE_UNAVAILABLE, e.toString());
                }
            });
        }
        else {
            exchange.respond(CoAP.ResponseCode.METHOD_NOT_ALLOWED, "Property writeOnly");
        }
    }

    @Override
    public void handlePUT(CoapExchange exchange) {
        log.info("CoapServer handles PUT to {}", getURI());

        String requestContentFormat = getOrDefaultRequestContentType(exchange);
        if (!ContentManager.isSupportedMediaType(requestContentFormat)) {
            log.warn("Unsupported media type: {}", requestContentFormat);
            String payload = "Unsupported Media Type (supported: " + String.join(", ", ContentManager.getSupportedMediaTypes()) + ")";
            exchange.respond(CoAP.ResponseCode.UNSUPPORTED_CONTENT_FORMAT, payload);
        }

        if (!property.isReadOnly()) {
            byte[] requestPayload = exchange.getRequestPayload();

            try {
                Object input = ContentManager.contentToValue(new Content(requestContentFormat, requestPayload), property);

                property.write(input).whenComplete((output, e) -> {
                    if (e == null) {
                        if (output != null) {
                            try {
                                Content outputContent = ContentManager.valueToContent(output, requestContentFormat);
                                int outputContentFormat = MediaTypeRegistry.parse(outputContent.getType());
                                exchange.respond(CoAP.ResponseCode.CHANGED, outputContent.getBody(), outputContentFormat);
                            }
                            catch (ContentCodecException ex) {
                                ex.printStackTrace();
                                exchange.respond(CoAP.ResponseCode.SERVICE_UNAVAILABLE, ex.toString());
                            }
                        }
                        else {
                            exchange.respond(CoAP.ResponseCode.CHANGED, new byte[0], MediaTypeRegistry.parse(requestContentFormat));
                        }
                    }
                    else {
                        e.printStackTrace();
                        exchange.respond(CoAP.ResponseCode.SERVICE_UNAVAILABLE, e.toString());
                    }
                });
            }
            catch (ContentCodecException ex) {
                ex.printStackTrace();
                exchange.respond(CoAP.ResponseCode.SERVICE_UNAVAILABLE, ex.toString());
            }
        }
        else {
            exchange.respond(CoAP.ResponseCode.METHOD_NOT_ALLOWED, "Property writeOnly");
        }
    }
}
