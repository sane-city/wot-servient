package city.sane.wot.binding.http.route;

import city.sane.wot.thing.ExposedThing;
import org.eclipse.jetty.http.HttpStatus;
import spark.Request;
import spark.Response;

import java.util.Map;

abstract class AbstractInteractionRoute extends AbstractRoute {
    protected final Map<String, ExposedThing> things;

    public AbstractInteractionRoute(Map<String, ExposedThing> things) {
        this.things = things;
    }

    @Override
    public Object handle(Request request, Response response) {
        logRequest(request);

        String requestContentType = getOrDefaultRequestContentType(request);

        String unsupportedMediaTypeResponse = unsupportedMediaTypeResponse(response, requestContentType);
        if (unsupportedMediaTypeResponse != null) {
            return unsupportedMediaTypeResponse;
        }

        String id = request.params(":id");
        String name = request.params(":name");

        ExposedThing thing = things.get(id);
        if (thing != null) {
            return handleInteraction(request, response, requestContentType, name, thing);
        }
        else {
            response.status(HttpStatus.NOT_FOUND_404);
            return "Thing not found";
        }
    }

    protected abstract Object handleInteraction(Request request,
                                                Response response,
                                                String requestContentType,
                                                String name,
                                                ExposedThing thing);
}
