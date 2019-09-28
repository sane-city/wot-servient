package city.sane.wot.binding.coap.resource;

import city.sane.wot.binding.coap.CoapServer;
import city.sane.wot.content.Content;
import city.sane.wot.content.ContentCodecException;
import city.sane.wot.content.ContentManager;
import city.sane.wot.thing.observer.Subscription;
import city.sane.wot.thing.property.ExposedThingProperty;
import org.eclipse.californium.core.coap.CoAP;
import org.eclipse.californium.core.coap.MediaTypeRegistry;
import org.eclipse.californium.core.observe.ObserveRelation;
import org.eclipse.californium.core.server.resources.CoapExchange;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Endpoint for subscribing to value changes for a {@link city.sane.wot.thing.property.ThingProperty}.
 */
public class ObservePropertyResource extends AbstractResource {
    final static Logger log = LoggerFactory.getLogger(ObservePropertyResource.class);
    private final CoapServer server;
    private final String name;
    private final ExposedThingProperty property;
    private Subscription subscription = null;
    private Object data;
    private Throwable e;

    public ObservePropertyResource(CoapServer server, String name, ExposedThingProperty property) {
        super("observable");
        this.server = server;
        this.name = name;
        this.property = property;

        setObservable(true); // enable observing
        setObserveType(CoAP.Type.CON); // configure the notification type to CONs
        getAttributes().setObservable(); // mark observable in the Link-Format
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

        if (exchange.getRequestOptions().getObserve() != null) {
            ensureSubcription();

            if (!exchange.advanced().getRequest().isAcknowledged()) {
                // The requestor should only be informed about new values.
                // send acknowledgement
                exchange.accept();

                ObserveRelation relation = exchange.advanced().getRelation();
                relation.setEstablished(true);
                addObserveRelation(relation);
            }
            else {
                if (e == null) {
                    try {
                        log.debug("New data received for Property '{}': {}", name, data);

                        Content content = ContentManager.valueToContent(data, requestContentFormat);

                        int contentFormat = MediaTypeRegistry.parse(content.getType());
                        byte[] body = content.getBody();
                        if (body.length > 0) {
                            exchange.respond(CoAP.ResponseCode.CONTENT, body, contentFormat);
                        }
                        else {
                            exchange.respond(CoAP.ResponseCode.CONTENT);
                        }
                    }
                    catch (ContentCodecException e) {
                        log.warn("Cannot process data for Property '{}': {}", name, e.toString());
                        e.printStackTrace();
                        exchange.respond(CoAP.ResponseCode.SERVICE_UNAVAILABLE, "Invalid Event Data");
                    }
                }
                else {
                    log.warn("Cannot process data for Property '{}': {}", name, e.toString());
                    e.printStackTrace();
                    exchange.respond(CoAP.ResponseCode.SERVICE_UNAVAILABLE, "Invalid Event Data");
                }
            }
        }
        else {
            log.warn("Reject request: Observe Option is missing");
            exchange.respond(CoAP.ResponseCode.BAD_REQUEST, "No Observe Option");
        }
    }

    synchronized private void ensureSubcription() {
        if (subscription == null) {
            subscription = property.subscribe(
                    data -> changeResource(data, null),
                    e -> changeResource(null, e),
                    () -> {
                    }
            );
        }
    }

    synchronized private void changeResource(Object data, Throwable e) {
        this.data = data;
        this.e = e;
        changed();
    }
}
