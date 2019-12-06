package city.sane.wot.binding.http.route;

import city.sane.wot.content.Content;
import city.sane.wot.content.ContentCodecException;
import city.sane.wot.content.ContentManager;
import city.sane.wot.thing.ExposedThing;
import city.sane.wot.thing.action.ExposedThingAction;
import org.eclipse.jetty.http.HttpStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import spark.Request;
import spark.Response;

import java.util.HashMap;
import java.util.Map;

/**
 * Endpoint for invoking a {@link city.sane.wot.thing.action.ThingAction}.
 */
public class InvokeActionRoute extends AbstractRoute {
    static final Logger log = LoggerFactory.getLogger(InvokeActionRoute.class);

    private final Map<String, ExposedThing> things;

    public InvokeActionRoute(Map<String, ExposedThing> things) {
        this.things = things;
    }

    @Override
    public Object handle(Request request, Response response) throws Exception {
        log.info("Handle {} to '{}'", request.requestMethod(), request.url());
        if (request.raw().getQueryString() != null && !request.raw().getQueryString().isEmpty()) {
            log.info("Request parameters: {}", request.raw().getQueryString());
        }

        String requestContentType = getOrDefaultRequestContentType(request);

        String unsupportedMediaTypeResponse = unsupportedMediaTypeResponse(response, requestContentType);
        if (unsupportedMediaTypeResponse != null) {
            return unsupportedMediaTypeResponse;
        }

        String id = request.params(":id");
        String name = request.params(":name");

        ExposedThing thing = things.get(id);
        if (thing != null) {
            ExposedThingAction action = thing.getAction(name);
            if (action != null) {
                try {
                    Content content = new Content(requestContentType, request.bodyAsBytes());
                    Object input = ContentManager.contentToValue(content, action.getInput());

                    Map<String, Object> options = Map.of(
                            "uriVariables", parseUrlParameters(request.queryMap().toMap(), action.getUriVariables())
                    );

                    Object value = action.invoke(input, options).get();

                    return respondWithValue(response, requestContentType, content, value);
                }
                catch (ContentCodecException e) {
                    response.status(HttpStatus.SERVICE_UNAVAILABLE_503);
                    return e;
                }
            }
            else {
                response.status(HttpStatus.NOT_FOUND_404);
                return "Action not found";
            }
        }
        else {
            response.status(HttpStatus.NOT_FOUND_404);
            return "Thing not found";
        }
    }

    private Object respondWithValue(Response response, String requestContentType, Content content, Object value) {
        try {
            Content outputContent = ContentManager.valueToContent(value, requestContentType);

            if (value != null) {
                response.type(content.getType());
                return outputContent;
            }
            else {
                return "";
            }
        }
        catch (ContentCodecException e) {
            response.status(HttpStatus.SERVICE_UNAVAILABLE_503);
            return e;
        }
    }

    private Map<String, Object> parseUrlParameters(Map<String, String[]> urlParams, Map<String, Map> uriVariables) {
        log.debug("parse url parameters '{}' with uri variables '{}'", urlParams.keySet(), uriVariables);
        Map<String, Object> params = new HashMap<>();
        for (Map.Entry<String, String[]> entry : urlParams.entrySet()) {
            String name = entry.getKey();
            String[] urlValue = entry.getValue();

            Map uriVariable = uriVariables.get(name);
            if (uriVariable != null) {
                Object type = uriVariable.get("type");

                if (type != null) {
                    if (type.equals("integer") || type.equals("number")) {
                        Integer value = Integer.valueOf(urlValue[0]);
                        params.put(name, value);
                    }
                    else if (type.equals("string")) {
                        String value = urlValue[0];
                        params.put(name, value);
                    }
                    else {
                        log.warn("Not able to read variable '{}' because variable type '{}' is unknown", name, type);
                    }
                }
            }
        }
        return params;
    }
}
