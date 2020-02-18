package city.sane.wot.thing.schema;

import java.util.List;

/**
 * Describes data of type <a href="https://www.w3.org/TR/wot-thing-description/#arrayschema">Array</a>.
 */
public class ArraySchema extends AbstractDataSchema<List> {
    public static final String TYPE = "array";
    public static final Class<List> CLASS_TYPE = List.class;

    @Override
    public String getType() {
        return TYPE;
    }

    @Override
    public Class<List> getClassType() {
        return CLASS_TYPE;
    }

    @Override
    public String toString() {
        return "ArraySchema{}";
    }
}
