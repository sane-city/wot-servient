package city.sane.wot.binding.http.route;

import city.sane.wot.content.Content;
import city.sane.wot.content.ContentCodecException;
import city.sane.wot.content.ContentManager;
import city.sane.wot.thing.ExposedThing;
import org.eclipse.jetty.http.HttpStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import spark.Request;
import spark.Response;

import java.util.Map;

/**
 * Endpoint for reading all properties from a Thing
 */
public class ReadAllPropertiesRoute extends AbstractRoute {
    static final Logger log = LoggerFactory.getLogger(ReadAllPropertiesRoute.class);
    private final Map<String, ExposedThing> things;

    public ReadAllPropertiesRoute(Map<String, ExposedThing> things) {
        this.things = things;
    }

    @Override
    public Object handle(Request request, Response response) throws Exception {
        logRequest(request);

        String requestContentType = getOrDefaultRequestContentType(request);

        String unsupportedMediaTypeResponse = unsupportedMediaTypeResponse(response, requestContentType);
        if (unsupportedMediaTypeResponse != null) {
            return unsupportedMediaTypeResponse;
        }

        String id = request.params(":id");

        ExposedThing thing = things.get(id);
        if (thing != null) {
            Map<String, Object> values = thing.readProperties().get();

            // remove writeOnly properties
            values.entrySet().removeIf(entry -> thing.getProperty(entry.getKey()).isWriteOnly());

            try {
                Content content = ContentManager.valueToContent(values, requestContentType);
                response.type(content.getType());
                return content;
            }
            catch (ContentCodecException e) {
                response.status(HttpStatus.SERVICE_UNAVAILABLE_503);
                return e;
            }
        }
        else {
            response.status(HttpStatus.NOT_FOUND_404);
            return "Thing not found";
        }
    }
}
