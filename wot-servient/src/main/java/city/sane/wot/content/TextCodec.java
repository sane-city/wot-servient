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
package city.sane.wot.content;

import city.sane.wot.thing.schema.BooleanSchema;
import city.sane.wot.thing.schema.DataSchema;
import city.sane.wot.thing.schema.IntegerSchema;
import city.sane.wot.thing.schema.NumberSchema;
import city.sane.wot.thing.schema.StringSchema;

import java.nio.charset.Charset;
import java.util.Map;

/**
 * (De)serializes data in plaintext format.
 */
public class TextCodec implements ContentCodec {
    @Override
    public String getMediaType() {
        return "text/plain";
    }

    @Override
    public <T> T bytesToValue(byte[] body, DataSchema<T> schema, Map<String, String> parameters) {
        String charset = parameters.get("charset");

        String parsed;
        if (charset != null) {
            parsed = new String(body, Charset.forName(charset));
        }
        else {
            parsed = new String(body);
        }

        String type = schema.getType();
        // TODO: array, object
        switch (type) {
            case BooleanSchema
                    .TYPE:
                return (T) Boolean.valueOf(parsed);
            case IntegerSchema
                    .TYPE:
                return (T) Integer.valueOf(parsed);
            case NumberSchema
                    .TYPE:
                if (parsed.contains(".")) {
                    return (T) Double.valueOf(parsed);
                }
                else {
                    return (T) Long.valueOf(parsed);
                }
            case StringSchema
                    .TYPE:
                return (T) parsed;
            default:
                return null;
        }
    }

    @Override
    public byte[] valueToBytes(Object value, Map<String, String> parameters) {
        String charset = parameters.get("charset");

        if (charset != null) {
            return value.toString().getBytes(Charset.forName(charset));
        }
        else {
            return value.toString().getBytes();
        }
    }
}
