package city.sane.wot.thing.schema;

/**
 * Describes data of type <a href="https://www.w3.org/TR/wot-thing-description/#numberschema">number</a>.
 */
public class NumberSchema extends AbstractDataSchema<Number> {
    public static final String TYPE = "number";
    public static final Class<Number> CLASS_TYPE = Number.class;

    @Override
    public String getType() {
        return TYPE;
    }

    @Override
    public Class<Number> getClassType() {
        return CLASS_TYPE;
    }

    @Override
    public String toString() {
        return "NumberSchema{}";
    }
}
