package city.sane.wot.binding.http.route;

import city.sane.wot.content.ContentManager;
import spark.Request;
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
}
