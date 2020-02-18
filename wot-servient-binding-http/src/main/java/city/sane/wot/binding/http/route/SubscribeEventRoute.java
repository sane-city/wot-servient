package city.sane.wot.binding.http.route;

import city.sane.wot.content.Content;
import city.sane.wot.content.ContentCodecException;
import city.sane.wot.content.ContentManager;
import city.sane.wot.thing.ExposedThing;
import city.sane.wot.thing.event.ExposedThingEvent;
import city.sane.wot.thing.observer.Subscription;
import org.eclipse.jetty.http.HttpStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import spark.Request;
import spark.Response;

import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

/**
 * Endpoint for interaction with a {@link city.sane.wot.thing.event.ThingEvent}.
 */
public class SubscribeEventRoute extends AbstractInteractionRoute {
    private static final Logger log = LoggerFactory.getLogger(SubscribeEventRoute.class);

    public SubscribeEventRoute(Map<String, ExposedThing> things) {
        super(things);
    }

    @Override
    protected Object handleInteraction(Request request,
                                       Response response,
                                       String requestContentType,
                                       String name,
                                       ExposedThing thing) {
        ExposedThingEvent event = thing.getEvent(name);
        if (event != null) {
            CompletableFuture<Object> result = new CompletableFuture();
            Subscription subscription = event.subscribe(
                    data -> {
                        log.debug("Next data received for Event connection");
                        try {
                            Content content = ContentManager.valueToContent(data, requestContentType);
                            result.complete(content);
                        }
                        catch (ContentCodecException e) {
                            log.warn("Cannot process data for Event '{}': {}", name, e);
                            response.status(HttpStatus.SERVICE_UNAVAILABLE_503);
                            result.complete("Invalid Event Data");
                        }
                    },
                    e -> {
                        response.status(HttpStatus.SERVICE_UNAVAILABLE_503);
                        result.complete(e);
                    },
                    () -> result.complete("")
            );
            log.debug("Subscription created");

            result.whenComplete((r, e) -> {
                log.debug("Closes Event connection");
                subscription.unsubscribe();
            });

            try {
                return result.get();
            }
            catch (InterruptedException | ExecutionException e) {
                response.status(HttpStatus.SERVICE_UNAVAILABLE_503);
                return e;
            }
        }
        else {
            response.status(HttpStatus.NOT_FOUND_404);
            return "Event not found";
        }
    }
}
