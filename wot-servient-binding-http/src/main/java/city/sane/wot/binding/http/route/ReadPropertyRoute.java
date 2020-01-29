package city.sane.wot.binding.http.route;

import city.sane.wot.content.Content;
import city.sane.wot.content.ContentCodecException;
import city.sane.wot.content.ContentManager;
import city.sane.wot.thing.ExposedThing;
import city.sane.wot.thing.property.ExposedThingProperty;
import org.eclipse.jetty.http.HttpStatus;
import spark.Request;
import spark.Response;

import java.util.Map;
import java.util.concurrent.ExecutionException;

/**
 * Endpoint for reading values from a {@link city.sane.wot.thing.property.ThingProperty}.
 */
public class ReadPropertyRoute extends AbstractInteractionRoute {
    public ReadPropertyRoute(Map<String, ExposedThing> things) {
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
            if (!property.isWriteOnly()) {

                try {
                    Object value = property.read().get();

                    Content content = ContentManager.valueToContent(value, requestContentType);
                    response.type(content.getType());
                    return content;
                }
                catch (ContentCodecException | InterruptedException | ExecutionException e) {
                    response.status(HttpStatus.SERVICE_UNAVAILABLE_503);
                    return e;
                }
            }
            else {
                response.status(HttpStatus.BAD_REQUEST_400);
                return "Property writeOnly";
            }
        }
        else {
            response.status(HttpStatus.NOT_FOUND_404);
            return "Property not found";
        }
    }

}
