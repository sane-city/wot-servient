package city.sane.wot.thing.schema;

/**
 * Describes data of type <a href="https://www.w3.org/TR/wot-thing-description/#nullschema">null</a>.
 */
public class NullSchema extends AbstractDataSchema {
    public static final String TYPE = "null";
    public static final Class<Object> CLASS_TYPE = Object.class;

    @Override
    public String getType() {
        return TYPE;
    }

    @Override
    public Class getClassType() {
        return CLASS_TYPE;
    }

    @Override
    public String toString() {
        return "NullSchema{}";
    }
}
