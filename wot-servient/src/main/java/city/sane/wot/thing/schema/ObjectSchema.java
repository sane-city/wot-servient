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

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Describes data of type <a href="https://www.w3.org/TR/wot-thing-description/#objectschema">object</a>.
 */
public class ObjectSchema extends AbstractDataSchema<Map> {
    public static final String TYPE = "object";
    public static final Class<Map> CLASS_TYPE = Map.class;
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private final Map<String, DataSchema> properties;
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private final List<String> required;

    public ObjectSchema() {
        this(new HashMap<>(), new ArrayList<>());
    }

    public ObjectSchema(Map<String, DataSchema> properties,
                        List<String> required) {
        this.properties = properties;
        this.required = required;
    }

    @Override
    public String getType() {
        return TYPE;
    }

    @Override
    public Class<Map> getClassType() {
        return CLASS_TYPE;
    }

    @Override
    public String toString() {
        return "ObjectSchema{}";
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), getProperties(), getRequired());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof ObjectSchema)) {
            return false;
        }
        if (!super.equals(o)) {
            return false;
        }
        ObjectSchema that = (ObjectSchema) o;
        return Objects.equals(getProperties(), that.getProperties()) &&
                Objects.equals(getRequired(), that.getRequired());
    }

    public Map<String, DataSchema> getProperties() {
        return properties;
    }

    public List<String> getRequired() {
        return required;
    }
}
