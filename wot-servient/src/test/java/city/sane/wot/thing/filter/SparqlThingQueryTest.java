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
        assertThrows(ThingQueryException.class, () -> {
            new SparqlThingQuery("?__id__ ?p ?o");
        });
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