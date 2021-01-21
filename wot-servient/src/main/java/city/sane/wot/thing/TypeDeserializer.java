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

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.TextNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Iterator;

/**
 * Deserializes the individual type or the list of types of a {@link Thing} from JSON. Is used by
 * Jackson
 */
public class TypeDeserializer extends JsonDeserializer<Type> {
    private static final Logger log = LoggerFactory.getLogger(TypeDeserializer.class);

    @Override
    public Type deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
        JsonToken t = p.currentToken();
        if (t == JsonToken.VALUE_STRING) {
            return new Type(p.getValueAsString());
        }
        else if (t == JsonToken.START_ARRAY) {
            Type type = new Type();

            ArrayNode arrayNode = p.getCodec().readTree(p);
            Iterator<JsonNode> arrayElements = arrayNode.elements();
            while (arrayElements.hasNext()) {
                JsonNode arrayElement = arrayElements.next();

                if (arrayElement instanceof TextNode) {
                    type.addType(arrayElement.asText());
                }
            }

            return type;
        }
        else {
            log.warn("Unable to deserialize Context of type '{}'", t);
            return null;
        }
    }
}
