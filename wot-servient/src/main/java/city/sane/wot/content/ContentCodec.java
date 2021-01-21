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

import city.sane.wot.thing.schema.DataSchema;

import java.util.Collections;
import java.util.Map;

/**
 * A ContentCodec is responsible for (de)serializing data in certain encoding (e.g. JSON, CBOR).
 */
interface ContentCodec {
    /**
     * Returns the media type supported by the codec (e.g. application/json).
     *
     * @return
     */
    String getMediaType();

    /**
     * Deserializes <code>body</code> according to the data schema defined in <code>schema</code>.
     *
     * @param body
     * @param schema
     * @param <T>
     * @return
     * @throws ContentCodecException
     */
    default <T> T bytesToValue(byte[] body, DataSchema<T> schema) throws ContentCodecException {
        return bytesToValue(body, schema, Collections.emptyMap());
    }

    /**
     * Deserializes <code>body</code> according to the data schema defined in <code>schema</code>.
     * <code>parameters</code> can contain additional information about the encoding of the data
     * (e.g. the used character set).
     *
     * @param body
     * @param schema
     * @param parameters
     * @param <T>
     * @return
     * @throws ContentCodecException
     */
    <T> T bytesToValue(byte[] body,
                       DataSchema<T> schema,
                       Map<String, String> parameters) throws ContentCodecException;

    /**
     * Serialized <code>value</code> according to the data schema defined in <code>schema</code> to
     * a byte array.
     *
     * @param value
     * @return
     * @throws ContentCodecException
     */
    default byte[] valueToBytes(Object value) throws ContentCodecException {
        return valueToBytes(value, Collections.emptyMap());
    }

    /**
     * Serialized <code>value</code> according to the data schema defined in <code>schema</code> to
     * a byte array. <code>parameters</code> can contain additional information about the encoding
     * of the data (e.g. the used character set).
     *
     * @param value
     * @param parameters
     * @return
     * @throws ContentCodecException
     */
    byte[] valueToBytes(Object value, Map<String, String> parameters) throws ContentCodecException;
}
