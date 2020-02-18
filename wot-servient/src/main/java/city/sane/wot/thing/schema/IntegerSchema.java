package city.sane.wot.thing.schema;

/**
 * Describes data of type <a href="https://www.w3.org/TR/wot-thing-description/#integerschema">integer</a>.
 */
public class IntegerSchema extends AbstractDataSchema<Integer> {
    public static final String TYPE = "integer";
    public static final Class<Integer> CLASS_TYPE = Integer.class;

    @Override
    public String getType() {
        return TYPE;
    }

    @Override
    public Class<Integer> getClassType() {
        return CLASS_TYPE;
    }

    @Override
    public String toString() {
        return "IntegerSchema{}";
    }
}
