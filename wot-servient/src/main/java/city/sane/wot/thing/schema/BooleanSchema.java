package city.sane.wot.thing.schema;

/**
 * Describes data of type <a href="https://www.w3.org/TR/wot-thing-description/#booleanschema">boolean</a>.
 */
public class BooleanSchema extends AbstractDataSchema<Boolean> {
    public static final String TYPE = "boolean";
    public static final Class<Boolean> CLASS_TYPE = Boolean.class;

    @Override
    public String getType() {
        return TYPE;
    }

    @Override
    public Class<Boolean> getClassType() {
        return CLASS_TYPE;
    }

    @Override
    public String toString() {
        return "BooleanSchema{}";
    }
}
