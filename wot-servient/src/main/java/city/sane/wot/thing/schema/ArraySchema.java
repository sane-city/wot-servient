package city.sane.wot.thing.schema;

import java.util.List;

/**
 * Describes data of type <a href="https://www.w3.org/TR/wot-thing-description/#arrayschema">Array</a>.
 */
public class ArraySchema extends AbstractDataSchema<List> {
    @Override
    public String getType() {
        return "array";
    }

    @Override
    public Class<List> getClassType() {
        return List.class;
    }

    @Override
    public String toString() {
        return "ArraySchema{}";
    }
}
