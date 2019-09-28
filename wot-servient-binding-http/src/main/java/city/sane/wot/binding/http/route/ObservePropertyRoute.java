package city.sane.wot.binding.http.route;

import city.sane.wot.content.Content;
import city.sane.wot.content.ContentCodecException;
import city.sane.wot.content.ContentManager;
import city.sane.wot.thing.ExposedThing;
import city.sane.wot.thing.observer.Subscription;
import city.sane.wot.thing.property.ExposedThingProperty;
import org.eclipse.jetty.http.HttpStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import spark.Request;
import spark.Response;

import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * Endpoint for subscribing to value changes for a {@link city.sane.wot.thing.property.ThingProperty}.
 */
public class ObservePropertyRoute extends AbstractRoute {
    final static Logger log = LoggerFactory.getLogger(ObservePropertyRoute.class);

    private final Map<String, ExposedThing> things;

    public ObservePropertyRoute(Map<String, ExposedThing> things) {
        this.things = things;
    }

    @Override
    public Object handle(Request request, Response response) throws Exception {
        log.info("Handle {} to '{}'", request.requestMethod(), request.url());

        String requestContentType = getOrDefaultRequestContentType(request);
        if (!ContentManager.isSupportedMediaType(requestContentType)) {
            log.warn("Unsupported media type: {}", requestContentType);
            response.status(HttpStatus.UNSUPPORTED_MEDIA_TYPE_415);
            return "Unsupported Media Type (supported: " + String.join(", ", ContentManager.getSupportedMediaTypes()) + ")";
        }

        String id = request.params(":id");
        String name = request.params(":name");

        ExposedThing thing = things.get(id);
        if (thing != null) {
            ExposedThingProperty property = thing.getProperty(name);
            if (property != null) {
                if (!property.isWriteOnly() && property.isObservable()) {
                    CompletableFuture<Object> result = new CompletableFuture();
                    Subscription subscription = property.subscribe(
                            data -> {
                                log.debug("Next data received for Property connection");
                                try {
                                    Content content = ContentManager.valueToContent(data, requestContentType);
                                    response.type(content.getType());
                                    result.complete(content);
                                }
                                catch (ContentCodecException e) {
                                    log.warn("Cannot process data for Property '{}': {}", name, e.toString());
                                    response.status(HttpStatus.SERVICE_UNAVAILABLE_503);
                                    result.complete("Invalid Property Data");
                                }

                            },
                            e -> {
                                response.status(HttpStatus.SERVICE_UNAVAILABLE_503);
                                result.complete(e);
                            },
                            () -> result.complete("")
                    );

                    result.whenComplete((r, e) -> {
                        log.debug("Closes Property connection");
                        subscription.unsubscribe();
                    });

                    Object output = result.get();

                    return output;
                }
                else {
                    response.status(HttpStatus.BAD_REQUEST_400);
                    return "Property writeOnly/not observable";
                }
            }
            else {
                response.status(HttpStatus.NOT_FOUND_404);
                return "Property not found";
            }
        }
        else {
            response.status(HttpStatus.NOT_FOUND_404);
            return "Thing not found";
        }
    }
}
