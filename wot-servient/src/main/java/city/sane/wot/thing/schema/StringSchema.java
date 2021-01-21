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

/**
 * Describes data of type <a href="https://www.w3.org/TR/wot-thing-description/#stringschema">string</a>.
 */
public class StringSchema extends AbstractDataSchema<String> {
    public static final String TYPE = "string";
    public static final Class<String> CLASS_TYPE = String.class;

    @Override
    public String getType() {
        return TYPE;
    }

    @Override
    public Class<String> getClassType() {
        return CLASS_TYPE;
    }

    @Override
    public String toString() {
        return "StringSchema{}";
    }
}
