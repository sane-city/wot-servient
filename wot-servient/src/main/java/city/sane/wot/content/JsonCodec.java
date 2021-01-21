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
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.util.Map;

/**
 * (De)serializes data in JSON format.
 */
public class JsonCodec implements ContentCodec {
    private final ObjectMapper mapper = new ObjectMapper();

    @Override
    public String getMediaType() {
        return "application/json";
    }

    @Override
    public <T> T bytesToValue(byte[] body,
                              DataSchema<T> schema,
                              Map<String, String> parameters) throws ContentCodecException {
        try {
            return getMapper().readValue(body, schema.getClassType());
        }
        catch (IOException e) {
            throw new ContentCodecException("Failed to decode " + getMediaType() + ": " + e.toString());
        }
    }

    @Override
    public byte[] valueToBytes(Object value,
                               Map<String, String> parameters) throws ContentCodecException {
        try {
            return getMapper().writeValueAsBytes(value);
        }
        catch (JsonProcessingException e) {
            throw new ContentCodecException("Failed to encode " + getMediaType() + ": " + e.toString());
        }
    }

    ObjectMapper getMapper() {
        return mapper;
    }
}
