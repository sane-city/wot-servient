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
package city.sane.wot.thing.filter;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.instanceOf;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class ThingQueryTest {
    @Test
    public void jacksonShouldBeAbleToMapSparqlToCorrectImplementation() throws JsonProcessingException {
        String json = "{\"type\": \"sparql\", \"query\": \"foo bar\"}";
        ThingQuery query = new ObjectMapper().readValue(json, ThingQuery.class);

        assertThat(query, instanceOf(SparqlThingQuery.class));
        assertEquals("foo bar", ((SparqlThingQuery) query).getQuery());
    }

    @Test
    public void jacksonShouldBeAbleToMapJsonToCorrectImplementation() throws JsonProcessingException {
        String json = "{\"type\": \"json\", \"query\": \"{\\\"@type\\\":\\\"https:\\/\\/www.w3.org\\/2019\\/wot\\/td#Thing\\\"}\"}";
        ThingQuery query = new ObjectMapper().readValue(json, ThingQuery.class);

        assertThat(query, instanceOf(JsonThingQuery.class));
    }
}