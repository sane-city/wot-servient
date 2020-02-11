package city.sane.wot.thing.schema;

/**
 * Describes data of type <a href="https://www.w3.org/TR/wot-thing-description/#nullschema">null</a>.
 */
public class NullSchema extends AbstractDataSchema {
    @Override
    public String getType() {
        return "null";
    }

    @Override
    public Class getClassType() {
        return Object.class;
    }

    @Override
    public String toString() {
        return "NullSchema{}";
    }
}
