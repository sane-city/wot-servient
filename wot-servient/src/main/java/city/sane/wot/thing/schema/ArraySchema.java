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

import java.util.List;

/**
 * Describes data of type <a href="https://www.w3.org/TR/wot-thing-description/#arrayschema">Array</a>.
 */
public class ArraySchema extends AbstractDataSchema<List> {
    public static final String TYPE = "array";
    public static final Class<List> CLASS_TYPE = List.class;

    @Override
    public String getType() {
        return TYPE;
    }

    @Override
    public Class<List> getClassType() {
        return CLASS_TYPE;
    }

    @Override
    public String toString() {
        return "ArraySchema{}";
    }
}
