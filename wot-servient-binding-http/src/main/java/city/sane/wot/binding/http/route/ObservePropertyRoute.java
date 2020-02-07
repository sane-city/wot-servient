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
import java.util.concurrent.ExecutionException;

/**
 * Endpoint for subscribing to value changes for a {@link city.sane.wot.thing.property.ThingProperty}.
 */
public class ObservePropertyRoute extends AbstractInteractionRoute {
    private static final Logger log = LoggerFactory.getLogger(ObservePropertyRoute.class);

    public ObservePropertyRoute(Map<String, ExposedThing> things) {
        super(things);
    }

    @Override
    protected Object handleInteraction(Request request,
                                       Response response,
                                       String requestContentType,
                                       String name,
                                       ExposedThing thing) {
        ExposedThingProperty property = thing.getProperty(name);
        if (property != null) {
            if (!property.isWriteOnly() && property.isObservable()) {
                CompletableFuture<Object> result = subscribeForNextData(response, requestContentType, name, property);

                try {
                    return result.get();
                }
                catch (InterruptedException | ExecutionException e) {
                    response.status(HttpStatus.SERVICE_UNAVAILABLE_503);
                    return e;
                }
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

    private CompletableFuture<Object> subscribeForNextData(Response response,
                                                           String requestContentType,
                                                           String name,
                                                           ExposedThingProperty property) {
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
                        log.warn("Cannot process data for Property '{}': {}", name, e);
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

        return result;
    }
}
