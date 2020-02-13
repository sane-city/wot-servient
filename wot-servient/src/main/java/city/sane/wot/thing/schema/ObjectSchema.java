package city.sane.wot.thing.schema;

import java.util.Map;

/**
 * Describes data of type <a href="https://www.w3.org/TR/wot-thing-description/#objectschema">object</a>.
 */
public class ObjectSchema extends AbstractDataSchema<Map> {
    public static final String TYPE = "object";
    public static final Class<Map> CLASS_TYPE = Map.class;

    @Override
    public String getType() {
        return TYPE;
    }

    @Override
    public Class<Map> getClassType() {
        return CLASS_TYPE;
    }

    @Override
    public String toString() {
        return "ObjectSchema{}";
    }
}
