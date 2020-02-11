package city.sane.wot.binding.coap.resource;

import city.sane.wot.content.Content;
import city.sane.wot.content.ContentCodecException;
import city.sane.wot.content.ContentManager;
import city.sane.wot.thing.observer.Subscribable;
import city.sane.wot.thing.observer.Subscription;
import org.eclipse.californium.core.coap.CoAP;
import org.eclipse.californium.core.coap.MediaTypeRegistry;
import org.eclipse.californium.core.observe.ObserveRelation;
import org.eclipse.californium.core.server.resources.CoapExchange;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

abstract class AbstractSubscriptionResource extends AbstractResource {
    private static final Logger log = LoggerFactory.getLogger(AbstractSubscriptionResource.class);
    private final String name;
    private final Subscribable<Object> subscribable;
    private Subscription subscription = null;
    private Object data;
    private Throwable e;

    AbstractSubscriptionResource(String resourceName,
                                 String name,
                                 Subscribable<Object> subscribable) {
        super(resourceName);

        this.name = name;
        this.subscribable = subscribable;

        setObservable(true); // enable observing
        setObserveType(CoAP.Type.CON); // configure the notification type to CONs
        getAttributes().setObservable(); // mark observable in the Link-Format
    }

    @Override
    public void handleGET(CoapExchange exchange) {
        log.debug("Handle GET to '{}'", getURI());

        String requestContentFormat = getOrDefaultRequestContentType(exchange);

        if (ensureSupportedContentFormat(exchange, requestContentFormat)) {
            if (exchange.getRequestOptions().getObserve() != null) {
                ensureSubscription();

                if (!exchange.advanced().getRequest().isAcknowledged()) {
                    // The requestor should only be informed about new values.
                    // send acknowledgement
                    exchange.accept();

                    ObserveRelation relation = exchange.advanced().getRelation();
                    relation.setEstablished(true);
                    addObserveRelation(relation);
                }
                else {
                    respondSubscriptionData(exchange, requestContentFormat);
                }
            }
            else {
                log.warn("Reject request: Observe Option is missing");
                exchange.respond(CoAP.ResponseCode.BAD_REQUEST, "No Observe Option");
            }
        }
    }

    private synchronized void ensureSubscription() {
        if (subscription == null) {
            subscription = subscribable.subscribe(
                    next -> changeResource(next, null),
                    ex -> changeResource(null, ex),
                    () -> {
                    }
            );
        }
    }

    private void respondSubscriptionData(CoapExchange exchange, String requestContentFormat) {
        String subscribableType = subscribable.getClass().getSimpleName();
        if (e == null) {
            try {
                log.debug("New data received for {} '{}': {}", subscribableType, name, data);
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
            catch (ContentCodecException ex) {
                log.warn("Cannot process data for {} '{}': {}", subscribableType, name, ex.getMessage());
                exchange.respond(CoAP.ResponseCode.SERVICE_UNAVAILABLE, "Invalid " + subscribableType + " Data");
            }
        }
        else {
            log.warn("Cannot process data for {} '{}': {}", subscribableType, name, e.getMessage());
            exchange.respond(CoAP.ResponseCode.SERVICE_UNAVAILABLE, "Invalid " + subscribableType + " Data");
        }
    }

    private synchronized void changeResource(Object data, Throwable e) {
        this.data = data;
        this.e = e;
        changed();
    }
}
