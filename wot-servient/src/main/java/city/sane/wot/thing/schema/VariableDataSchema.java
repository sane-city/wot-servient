package city.sane.wot.thing.schema;

import city.sane.ObjectBuilder;

/**
 * Describes data whose type is determined at runtime.
 */
public class VariableDataSchema implements DataSchema<Object> {
    protected String type;

    @Override
    public String getType() {
        return type;
    }

    @Override
    public Class getClassType() {
        return Object.class;
    }

    /**
     * Allows building new {@link VariableDataSchema} objects.
     */
    public static class Builder implements ObjectBuilder<VariableDataSchema> {
        private String type;

        public Builder setType(String type) {
            this.type = type;
            return this;
        }

        @Override
        public VariableDataSchema build() {
            VariableDataSchema schema = new VariableDataSchema();
            schema.type = type;
            return schema;
        }
    }
}
