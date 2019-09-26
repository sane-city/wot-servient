package city.sane.wot.thing.schema;

/**
 * Describes data of type <a href="https://www.w3.org/TR/wot-thing-description/#integerschema">integer</a>.
 */
public class IntegerSchema implements DataSchema<Integer> {
    @Override
    public String getType() {
        return "integer";
    }

    @Override
    public Class<Integer> getClassType() {
        return Integer.class;
    }
}
