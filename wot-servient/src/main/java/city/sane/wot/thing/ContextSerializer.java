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
package city.sane.wot.thing;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;

import java.io.IOException;
import java.util.Map;

/**
 * Serializes the single context or the list of contexts of a {@link Thing} to JSON. Is used by
 * Jackson
 */
class ContextSerializer extends JsonSerializer<Context> {
    @Override
    public void serialize(Context context,
                          JsonGenerator gen,
                          SerializerProvider serializers) throws IOException {
        String defaultUrl = context.getDefaultUrl();
        Map<String, String> prefixedUrls = context.getPrefixedUrls();

        boolean hasDefaultUrl = defaultUrl != null;
        boolean hasPrefixedUrls = prefixedUrls.size() > 0;
        if (hasDefaultUrl && hasPrefixedUrls) {
            gen.writeStartArray();
        }

        if (hasDefaultUrl) {
            gen.writeString(defaultUrl);
        }

        if (hasPrefixedUrls) {
            gen.writeObject(prefixedUrls);
        }

        if (hasDefaultUrl && hasPrefixedUrls) {
            gen.writeEndArray();
        }
    }
}
