package city.sane.wot.thing.filter;

import city.sane.wot.thing.Thing;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

import java.util.Collection;
import java.util.List;

/**
 * Is used in the discovery process and filters the things according to certain properties
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "type")
@JsonSubTypes({
        @JsonSubTypes.Type(value = JsonThingQuery.class, name = "json"),
        @JsonSubTypes.Type(value = SparqlThingQuery.class, name = "sparql")
})
public interface ThingQuery {
    /**
     * Applies the filter to the found things and returns only those things that meet the desired
     * criteria
     *
     * @param things
     * @return
     */
    List<Thing> filter(Collection<Thing> things);
}
