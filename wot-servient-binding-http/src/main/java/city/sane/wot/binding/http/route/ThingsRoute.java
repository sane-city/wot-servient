package city.sane.wot.binding.http.route;

import city.sane.wot.content.Content;
import city.sane.wot.content.ContentManager;
import city.sane.wot.thing.ExposedThing;
import spark.Request;
import spark.Response;

import java.util.Map;

/**
 * Endpoint for listing all Things from the {@link city.sane.wot.Servient}.
 */
public class ThingsRoute extends AbstractRoute {
    private final Map<String, ExposedThing> things;

    public ThingsRoute(Map<String, ExposedThing> things) {
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

        Content content = ContentManager.valueToContent(things, requestContentType);
        response.type(requestContentType);
        return content;
    }
}
