package city.sane.wot.binding.http;

import city.sane.wot.content.Content;
import spark.ResponseTransformer;

/**
 * Transformer for automatic conversion of {@link Content} objects to {@link String} objects.
 * Used by Spark HTTP server.
 */
public class ContentResponseTransformer implements ResponseTransformer {
    @Override
    public String render(Object model) {
        if (model instanceof Content) {
            Content content = (Content) model;
            return new String(content.getBody());
        }
        else if (model != null) {
            return model.toString();
        }
        else {
            return null;
        }
    }
}
