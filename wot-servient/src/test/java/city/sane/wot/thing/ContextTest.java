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

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class ContextTest {
    private final ObjectMapper jsonMapper = new ObjectMapper();

    @Test
    public void fromJson() throws IOException {
        // single value
        assertEquals(
                new Context("http://www.w3.org/ns/td"),
                jsonMapper.readValue("\"http://www.w3.org/ns/td\"", Context.class)
        );

        // array
        assertEquals(
                new Context("http://www.w3.org/ns/td"),
                jsonMapper.readValue("[\"http://www.w3.org/ns/td\"]", Context.class)
        );

        // multi type array
        assertEquals(
                new Context("http://www.w3.org/ns/td").addContext("saref", "https://w3id.org/saref#"),
                jsonMapper.readValue("[\"http://www.w3.org/ns/td\",{\"saref\":\"https://w3id.org/saref#\"}]", Context.class)
        );
    }

    @Test
    public void toJson() throws JsonProcessingException {
        // single value
        assertEquals(
                "\"http://www.w3.org/ns/td\"",
                jsonMapper.writeValueAsString(new Context("http://www.w3.org/ns/td"))
        );

        // multi type array
        assertEquals(
                "[\"http://www.w3.org/ns/td\",{\"saref\":\"https://w3id.org/saref#\"}]",
                jsonMapper.writeValueAsString(new Context("http://www.w3.org/ns/td").addContext("saref", "https://w3id.org/saref#"))
        );
    }
}