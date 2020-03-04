package city.sane.wot.thing.filter;

import city.sane.wot.thing.Context;
import city.sane.wot.thing.Thing;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static org.hamcrest.Matchers.matchesPattern;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

public class JsonThingQueryTest {
    @Test
    public void constructorShouldTranslateJsonQueryToCorrectSparqlQuery() throws ThingQueryException {
        JsonThingQuery query = new JsonThingQuery("{\"@type\":\"https://www.w3.org/2019/wot/td#Thing\"}");

        assertThat(
                query.getQuery().getQuery(),
                matchesPattern("\\?genid.* <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <https://www.w3.org/2019/wot/td#Thing> .\n")
        );
    }

    @Test
    public void filterType() throws ThingQueryException {
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
}