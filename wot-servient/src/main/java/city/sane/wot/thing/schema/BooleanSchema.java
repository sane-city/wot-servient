package city.sane.wot.thing.schema;

/**
 * Describes data of type <a href="https://www.w3.org/TR/wot-thing-description/#booleanschema">boolean</a>.
 */
public class BooleanSchema extends AbstractDataSchema<Boolean> {
    @Override
    public String getType() {
        return "boolean";
    }

    @Override
    public Class<Boolean> getClassType() {
        return Boolean.class;
    }
}
