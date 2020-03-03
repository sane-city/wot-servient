package city.sane.wot.thing.filter;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.instanceOf;
import static org.junit.Assert.assertEquals;

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