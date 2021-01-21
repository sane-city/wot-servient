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
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class SparqlThingQueryTest {
    @Test
    public void testToString() throws ThingQueryException {
        String queryString = "?x <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <https://www.w3.org/2019/wot/td##Thing> .";
        SparqlThingQuery query = new SparqlThingQuery(queryString);

        assertEquals("SparqlThingQuery{query='?x <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <https://www.w3.org/2019/wot/td##Thing> .'}", query.toString());
    }

    @Test
    public void filterById() throws ThingQueryException {
        List<Thing> things = new ArrayList<>();
        things.add(
                new Thing.Builder()
                        .setObjectContext(new Context("https://www.w3.org/2019/wot/td/v1"))
                        .setObjectType("Type")
                        .setId("KlimabotschafterWetterstationen:Stellingen")
                        .build()
        );
        for (int i = 0; i < 10; i++) {
            things.add(
                    new Thing.Builder()
                            .setObjectContext(new Context("https://www.w3.org/2019/wot/td/v1"))
                            .setObjectType("Type").setId("luftdaten.info:" + i)
                            .build()
            );
        }

        ThingQuery query = new SparqlThingQuery("?s ?p ?o.\n" +
                "FILTER (STRSTARTS(STR(?s), \"KlimabotschafterWetterstationen:\"))");
        Collection<Thing> filtered = query.filter(things);

        assertEquals(1, filtered.size());
    }

    @Test
    public void filterWarnOnUseOfReservedWords() {
        assertThrows(ThingQueryException.class, () -> new SparqlThingQuery("?__id__ ?p ?o"));
    }

    @Test
    public void filterWarnOnUseOfReservedWordsNegative() throws ThingQueryException {
        SparqlThingQuery query = new SparqlThingQuery("?idiotype ?p ?o");

        assertNotNull(query);
    }

    @Test
    public void testEquals() throws ThingQueryException {
        SparqlThingQuery queryA = new SparqlThingQuery("?x <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <https://www.w3.org/2019/wot/td##Thing> .");
        SparqlThingQuery queryB = new SparqlThingQuery("?x <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <https://www.w3.org/2019/wot/td##Thing> .");
        SparqlThingQuery queryC = new SparqlThingQuery("?s ?p ?o .");

        assertEquals(queryA, queryB);
        assertEquals(queryB, queryA);
        assertNotEquals(queryA, queryC);
        assertNotEquals(queryC, queryA);
    }
}