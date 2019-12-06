package city.sane.wot.thing.schema;

import java.util.Map;

/**
 * Describes data of type <a href="https://www.w3.org/TR/wot-thing-description/#objectschema">object</a>.
 */
public class ObjectSchema extends AbstractDataSchema<Map> {
    @Override
    public String getType() {
        return "object";
    }

    @Override
    public Class<Map> getClassType() {
        return Map.class;
    }
}
