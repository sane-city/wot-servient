package city.sane.wot.thing.filter;

import city.sane.wot.thing.Context;
import city.sane.wot.thing.Thing;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static org.junit.Assert.assertEquals;

public class JsonThingQueryTest {
    @Test
    public void filterType() throws ThingQueryException {
        List<Thing> things = new ArrayList<>();
        things.add(new Thing.Builder()
                .setObjectContexts(new Context("https://www.w3.org/2019/wot/td/v1"))
                .setObjectType("Thing")
                .setId("KlimabotschafterWetterstationen:Stellingen")
                .build());
        for (int i = 0; i < 10; i++) {
            things.add(new Thing.Builder()
                    .setObjectContexts(new Context("https://www.w3.org/2019/wot/td/v1"))
                    .setId("luftdaten.info:" + i)
                    .build());
        }

        ThingQuery query = new JsonThingQuery("{\"@type\":\"https://www.w3.org/2019/wot/td#Thing\"}");
        Collection<Thing> filtered = query.filter(things);

        assertEquals(1, filtered.size());
    }
}