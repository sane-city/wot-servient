package city.sane.wot.binding.http.route;

import city.sane.wot.Servient;
import city.sane.wot.content.Content;
import city.sane.wot.content.ContentCodecException;
import city.sane.wot.content.ContentManager;
import city.sane.wot.thing.ExposedThing;
import org.eclipse.jetty.http.HttpStatus;
import spark.Request;
import spark.Response;

import java.util.Map;
import java.util.concurrent.ExecutionException;

/**
 * Endpoint for reading all properties from a Thing
 */
public class ReadAllPropertiesRoute extends AbstractInteractionRoute {
    public ReadAllPropertiesRoute(Servient servient, String securityScheme,
                                  Map<String, ExposedThing> things) {
        super(servient, securityScheme, things);
    }

    @Override
    protected Object handleInteraction(Request request,
                                       Response response,
                                       String requestContentType,
                                       String name,
                                       ExposedThing thing) {
        try {
            Map<String, Object> values = thing.readProperties().get();

            // remove writeOnly properties
            values.entrySet().removeIf(entry -> thing.getProperty(entry.getKey()).isWriteOnly());

            Content content = ContentManager.valueToContent(values, requestContentType);
            response.type(content.getType());
            return content;
        }
        catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return null;
        }
        catch (ContentCodecException | ExecutionException e) {
            response.status(HttpStatus.SERVICE_UNAVAILABLE_503);
            return e;
        }
    }
}
