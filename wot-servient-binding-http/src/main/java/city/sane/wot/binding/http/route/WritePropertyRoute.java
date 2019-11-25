package city.sane.wot.binding.http.route;

import city.sane.wot.content.Content;
import city.sane.wot.content.ContentCodecException;
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
 * Endpoint for writing values to a {@link city.sane.wot.thing.property.ThingProperty}.
 */
public class WritePropertyRoute extends AbstractRoute {
    final static Logger log = LoggerFactory.getLogger(WritePropertyRoute.class);

    private final Map<String, ExposedThing> things;

    public WritePropertyRoute(Map<String, ExposedThing> things) {
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
                if (!property.isReadOnly()) {
                    try {
                        Content content = new Content(requestContentType, request.bodyAsBytes());
                        Object input = ContentManager.contentToValue(content, property);

                        Object output = property.write(input).get();
                        if (output != null) {
                            response.status(HttpStatus.OK_200);
                            return output;
                        } else {
                            response.status(HttpStatus.NO_CONTENT_204);
                            return "";
                        }
                    } catch (ContentCodecException e) {
                        response.status(HttpStatus.SERVICE_UNAVAILABLE_503);
                        return e;
                    }
                } else {
                    response.status(HttpStatus.BAD_REQUEST_400);
                    return "Property readOnly";
                }
            } else {
                response.status(HttpStatus.NOT_FOUND_404);
                return "Property not found";
            }
        } else {
            response.status(HttpStatus.NOT_FOUND_404);
            return "Thing not found";
        }
    }
}
