package city.sane.wot.thing;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;

import java.io.IOException;
import java.util.Map;

/**
 * Serializes the single context or the list of contexts of a {@link Thing} to JSON.
 * Is used by Jackson
 */
public class ContextSerializer extends JsonSerializer {
    @Override
    public void serialize(Object value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
        Context context = (Context) value;

        String defaultUrl = context.getDefaultUrl();
        Map<String, String> prefixedUrls = context.getPrefixedUrls();

        boolean hasDefaultUrl = defaultUrl != null;
        boolean hasPrefixedUrls = prefixedUrls.size() > 0;
        if (hasDefaultUrl && hasPrefixedUrls) {
            gen.writeStartArray();
        }

        if (hasDefaultUrl) {
            gen.writeString(defaultUrl);
        }

        if (hasPrefixedUrls) {
            gen.writeObject(prefixedUrls);
        }

        if (hasDefaultUrl && hasPrefixedUrls) {
            gen.writeEndArray();
        }
    }
}
