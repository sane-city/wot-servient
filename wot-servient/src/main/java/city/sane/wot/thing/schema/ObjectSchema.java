package city.sane.wot.thing.schema;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Describes data of type <a href="https://www.w3.org/TR/wot-thing-description/#objectschema">object</a>.
 */
public class ObjectSchema extends AbstractDataSchema<Map> {
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private final Map<String, DataSchema> properties;
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private final List<String> required;

    public ObjectSchema() {
        this(new HashMap<>(), new ArrayList<>());
    }

    public ObjectSchema(Map<String, DataSchema> properties,
                        List<String> required) {
        this.properties = properties;
        this.required = required;
    }

    @Override
    public String getType() {
        return "object";
    }

    @Override
    public Class<Map> getClassType() {
        return Map.class;
    }

    @Override
    public String toString() {
        return "ObjectSchema{}";
    }

    public Map<String, DataSchema> getProperties() {
        return properties;
    }

    public List<String> getRequired() {
        return required;
    }
}
