package city.sane.wot.thing.schema;

/**
 * Describes data of type <a href="https://www.w3.org/TR/wot-thing-description/#stringschema">string</a>.
 */
public class StringSchema extends AbstractDataSchema<String> {
    public static final String TYPE = "string";
    public static final Class<String> CLASS_TYPE = String.class;

    @Override
    public String getType() {
        return TYPE;
    }

    @Override
    public Class<String> getClassType() {
        return CLASS_TYPE;
    }

    @Override
    public String toString() {
        return "StringSchema{}";
    }
}
