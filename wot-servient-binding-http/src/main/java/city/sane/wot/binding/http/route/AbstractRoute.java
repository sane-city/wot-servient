package city.sane.wot.binding.http.route;

import city.sane.wot.content.ContentManager;
import org.eclipse.jetty.http.HttpStatus;
import spark.Request;
import spark.Response;
import spark.Route;

/**
 * Abstract route for exposing Things. Inherited from all other routes.
 */
public abstract class AbstractRoute implements Route {
    protected String getOrDefaultRequestContentType(Request request) {
        if (request.contentType() != null) {
            return request.contentType();
        }
        else {
            return ContentManager.DEFAULT;
        }
    }

    protected String unsupportedMediaTypeResponse(Response response, String requestContentType) {
        if (!ContentManager.isSupportedMediaType(requestContentType)) {
            ReadAllPropertiesRoute.log.warn("Unsupported media type: {}", requestContentType);
            response.status(HttpStatus.UNSUPPORTED_MEDIA_TYPE_415);
            return "Unsupported Media Type (supported: " + String.join(", ", ContentManager.getSupportedMediaTypes()) + ")";
        }
        else {
            return null;
        }
    }
}
