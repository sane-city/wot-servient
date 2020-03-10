package city.sane.wot.binding.http.route;

import city.sane.wot.content.Content;
import city.sane.wot.content.ContentManager;
import city.sane.wot.thing.ExposedThing;
import spark.Request;
import spark.Response;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Endpoint for listing all Things from the {@link city.sane.wot.Servient}.
 */
public class ThingsRoute extends AbstractRoute {
    private final List<String> addresses;
    private final Map<String, ExposedThing> things;

    public ThingsRoute(List<String> addresses,
                       Map<String, ExposedThing> things) {
        this.addresses = addresses;
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

        List<String> myThings = new ArrayList<>();
        for (String address : addresses) {
            for (String id : things.keySet()) {
                myThings.add(address + "/" + id);
            }
        }

        Content content = ContentManager.valueToContent(myThings, requestContentType);
        response.type(requestContentType);
        return content;
    }
}
