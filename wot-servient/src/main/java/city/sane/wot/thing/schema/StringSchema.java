package city.sane.wot.thing.schema;

/**
 * Describes data of type <a href="https://www.w3.org/TR/wot-thing-description/#stringschema">string</a>.
 */
public class StringSchema implements DataSchema<String> {
    @Override
    public String getType() {
        return "string";
    }

    @Override
    public Class<String> getClassType() {
        return String.class;
    }
}
