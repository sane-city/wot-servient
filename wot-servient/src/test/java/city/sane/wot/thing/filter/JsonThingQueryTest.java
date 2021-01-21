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

import city.sane.wot.thing.Context;
import city.sane.wot.thing.Thing;
import org.hamcrest.MatcherAssert;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static org.hamcrest.Matchers.matchesPattern;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

public class JsonThingQueryTest {
    @Test
    public void constructorShouldTranslateJsonQueryToCorrectSparqlQuery() throws ThingQueryException {
        JsonThingQuery query = new JsonThingQuery("{\"@type\":\"https://www.w3.org/2019/wot/td#Thing\"}");

        MatcherAssert.assertThat(
                query.getSparqlQuery().getQuery(),
                matchesPattern("\\?genid.* <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <https://www.w3.org/2019/wot/td#Thing> .\n")
        );
    }

    @Test
    public void filterShouldReturnOnlyMatchingThings() throws ThingQueryException {
        List<Thing> things = new ArrayList<>();
        things.add(new Thing.Builder()
                .setObjectContext(new Context("https://www.w3.org/2019/wot/td/v1"))
                .setObjectType("Thing")
                .setId("KlimabotschafterWetterstationen:Stellingen")
                .build());
        for (int i = 0; i < 10; i++) {
            things.add(new Thing.Builder()
                    .setObjectContext(new Context("https://www.w3.org/2019/wot/td/v1"))
                    .setId("luftdaten.info:" + i)
                    .build());
        }

        ThingQuery query = new JsonThingQuery("{\"@type\":\"https://www.w3.org/2019/wot/td#Thing\"}");
        Collection<Thing> filtered = query.filter(things);

        assertEquals(1, filtered.size());
    }

    @Test
    public void testEquals() {
        JsonThingQuery queryA = new JsonThingQuery("{\"@type\":\"https://www.w3.org/2019/wot/td#Thing\"}");
        JsonThingQuery queryB = new JsonThingQuery("{\"@type\":\"https://www.w3.org/2019/wot/td#Thing\"}");
        JsonThingQuery queryC = new JsonThingQuery("{\"http://www.w3.org/ns/td#title\":\"Klimabotschafter:Rahlstedt\"}");

        assertEquals(queryA, queryB);
        assertEquals(queryB, queryA);
        assertNotEquals(queryA, queryC);
        assertNotEquals(queryC, queryA);
    }
}