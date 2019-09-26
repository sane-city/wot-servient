package city.sane.wot.thing.schema;

/**
 * Describes data of type <a href="https://www.w3.org/TR/wot-thing-description/#nullschema">null</a>.
 */
public class NullSchema implements DataSchema {
    @Override
    public String getType() {
        return "null";
    }

    @Override
    public Class getClassType() {
        return Object.class;
    }
}
