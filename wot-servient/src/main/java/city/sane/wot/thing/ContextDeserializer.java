package city.sane.wot.thing;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.TextNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Iterator;
import java.util.Map;

/**
 * Deserializes the individual context or the list of contexts of a {@link Thing} from JSON.
 * Is used by Jackson
 */
public class ContextDeserializer extends JsonDeserializer {
    final static Logger log = LoggerFactory.getLogger(ContextDeserializer.class);

    @Override
    public Object deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
        JsonToken t = p.currentToken();
        if (t == JsonToken.VALUE_STRING) {
            return new Context(p.getValueAsString());
        }
        else if (t == JsonToken.START_ARRAY) {
            Context context = new Context();

            ArrayNode arrayNode = p.getCodec().readTree(p);
            Iterator<JsonNode> arrayElements = arrayNode.elements();
            while (arrayElements.hasNext()) {
                JsonNode arrayElement = arrayElements.next();

                if (arrayElement instanceof TextNode) {
                    context.addContext(arrayElement.asText());
                }
                else if (arrayElement instanceof ObjectNode) {
                    Iterator<Map.Entry<String, JsonNode>> objectEntries = arrayElement.fields();
                    while (objectEntries.hasNext()) {
                        Map.Entry<String, JsonNode> objectEntry = objectEntries.next();
                        String prefix = objectEntry.getKey();
                        String url = objectEntry.getValue().asText();
                        context.addContext(prefix, url);
                    }
                }
            }

            return context;
        }
        else {
            log.warn("Unable to deserialize Context of type '{}'", t);
            return null;
        }
    }
}
