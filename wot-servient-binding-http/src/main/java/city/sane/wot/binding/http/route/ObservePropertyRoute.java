package city.sane.wot.binding.http.route;

import city.sane.wot.Servient;
import city.sane.wot.content.Content;
import city.sane.wot.content.ContentManager;
import city.sane.wot.thing.ExposedThing;
import city.sane.wot.thing.property.ExposedThingProperty;
import org.eclipse.jetty.http.HttpStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import spark.Request;
import spark.Response;

import java.util.Map;

/**
 * Endpoint for subscribing to value changes for a {@link city.sane.wot.thing.property.ThingProperty}.
 */
public class ObservePropertyRoute extends AbstractInteractionRoute {
    private static final Logger log = LoggerFactory.getLogger(ObservePropertyRoute.class);

    public ObservePropertyRoute(Servient servient, String securityScheme,
                                Map<String, ExposedThing> things) {
        super(servient, securityScheme, things);
    }

    @Override
    protected Object handleInteraction(Request request,
                                       Response response,
                                       String requestContentType,
                                       String name,
                                       ExposedThing thing) {
        ExposedThingProperty<Object> property = thing.getProperty(name);
        if (property != null) {
            if (!property.isWriteOnly() && property.isObservable()) {
                Content content = property.observer()
                        .map(optional -> ContentManager.valueToContent(optional.orElse(null), requestContentType))
                        .firstElement().blockingGet();

                if (content != null) {
                    log.warn("Next data received for Event '{}': {}", name, content);
                    response.type(content.getType());
                    return content;
                }
                else {
                    response.status(HttpStatus.SERVICE_UNAVAILABLE_503);
                    return "";
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
}
