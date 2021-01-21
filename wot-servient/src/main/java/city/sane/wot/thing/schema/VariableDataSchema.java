/*
 * Copyright (c) 2021.
 *
 * This file is part of SANE Web of Things Servient.
 *
 * SANE Web of Things Servient is free software: you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * SANE Web of Things Servient is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with SANE Web of Things Servient.  If not, see
 * <http://www.gnu.org/licenses/>.
 */
package city.sane.wot.thing.schema;

import city.sane.ObjectBuilder;

/**
 * Describes data whose type is determined at runtime.
 */
public class VariableDataSchema extends AbstractDataSchema<Object> {
    private String type;

    @Override
    public int hashCode() {
        return super.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        return super.equals(obj);
    }

    @Override
    public String getType() {
        return type;
    }

    @Override
    public Class getClassType() {
        switch (type) {
            case ArraySchema.TYPE:
                return ArraySchema.CLASS_TYPE;
            case BooleanSchema.TYPE:
                return BooleanSchema.CLASS_TYPE;
            case IntegerSchema.TYPE:
                return IntegerSchema.CLASS_TYPE;
            case NullSchema.TYPE:
                return NullSchema.CLASS_TYPE;
            case NumberSchema.TYPE:
                return NumberSchema.CLASS_TYPE;
            case ObjectSchema.TYPE:
                return ObjectSchema.CLASS_TYPE;
            default:
                return StringSchema.CLASS_TYPE;
        }
    }

    @Override
    public String toString() {
        return "VariableDataSchema{" +
                "type='" + type + '\'' +
                '}';
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
