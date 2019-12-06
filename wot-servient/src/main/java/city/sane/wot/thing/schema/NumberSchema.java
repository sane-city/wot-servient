package city.sane.wot.thing.schema;

/**
 * Describes data of type <a href="https://www.w3.org/TR/wot-thing-description/#numberschema">number</a>.
 */
public class NumberSchema extends AbstractDataSchema<Number> {
    @Override
    public String getType() {
        return "number";
    }

    @Override
    public Class<Number> getClassType() {
        return Number.class;
    }
}
