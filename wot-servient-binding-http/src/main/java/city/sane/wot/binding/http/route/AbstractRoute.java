package city.sane.wot.binding.http.route;

import city.sane.wot.content.ContentManager;
import org.eclipse.jetty.http.HttpStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import spark.Request;
import spark.Response;
import spark.Route;

/**
 * Abstract route for exposing Things. Inherited from all other routes.
 */
abstract class AbstractRoute implements Route {
    static final Logger log = LoggerFactory.getLogger(AbstractRoute.class);

    String getOrDefaultRequestContentType(Request request) {
        if (request.contentType() != null) {
            return request.contentType();
        }
        else {
            return ContentManager.DEFAULT;
        }
    }

    String unsupportedMediaTypeResponse(Response response, String requestContentType) {
        if (!ContentManager.isSupportedMediaType(requestContentType)) {
            log.warn("Unsupported media type: {}", requestContentType);
            response.status(HttpStatus.UNSUPPORTED_MEDIA_TYPE_415);
            return "Unsupported Media Type (supported: " + String.join(", ", ContentManager.getSupportedMediaTypes()) + ")";
        }
        else {
            return null;
        }
    }

    void logRequest(Request request) {
        if (log.isDebugEnabled()) {
            log.debug("Handle {} to '{}'", request.requestMethod(), request.url());
            if (request.raw().getQueryString() != null && !request.raw().getQueryString().isEmpty()) {
                log.debug("Request parameters: {}", request.raw().getQueryString());
            }
        }
    }
}
