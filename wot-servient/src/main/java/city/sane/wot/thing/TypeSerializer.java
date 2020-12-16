package city.sane.wot.thing;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;

import java.io.IOException;
import java.util.Set;

/**
 * Serializes the single type or the list of types of a {@link Thing} to JSON. Is used by Jackson
 */
public class TypeSerializer extends JsonSerializer<Type> {
    @Override
    public void serialize(Type type,
                          JsonGenerator gen,
                          SerializerProvider serializers) throws IOException {
        Set<String> types = type.getTypes();
        if (types.size() == 1) {
            gen.writeString(types.iterator().next());
        }
        else if (types.size() > 1) {
            gen.writeStartArray();
            for (String t : types) {
                gen.writeString(t);
            }
            gen.writeEndArray();
        }
    }
}
