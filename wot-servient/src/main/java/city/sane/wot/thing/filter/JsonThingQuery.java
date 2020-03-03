package city.sane.wot.thing.filter;

import city.sane.wot.thing.Thing;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.rio.RDFFormat;
import org.eclipse.rdf4j.rio.Rio;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.StringReader;
import java.util.Collection;
import java.util.List;

/**
 * Allows filtering of things during discovery process using a JSON query.
 * <p>
 * Example Query: {"@type":"https://www.w3.org/2019/wot/td#Thing"}
 * </p>
 */
public class JsonThingQuery implements ThingQuery {
    @JsonDeserialize(using = JsonThingQueryQueryDeserializer.class)
    @JsonTypeInfo(use = JsonTypeInfo.Id.NONE)
    private final SparqlThingQuery query;

    public JsonThingQuery(String jsonQueryString) throws ThingQueryException {
        this(new SparqlThingQuery(jsonToSparqlQuery(jsonQueryString)));
    }

    JsonThingQuery(SparqlThingQuery query) {
        this.query = query;
    }

    private static String jsonToSparqlQuery(String queryString) throws ThingQueryException {
        try {
            StringReader reader = new StringReader(queryString);
            Model frame = Rio.parse(reader, "", RDFFormat.JSONLD);
            ByteArrayOutputStream o = new ByteArrayOutputStream();
            Rio.write(frame, o, RDFFormat.NTRIPLES);

            return o.toString().replace("_:", "?");
        }
        catch (IOException e) {
            throw new ThingQueryException(e);
        }
    }

    JsonThingQuery() {
        // required by jackson
        query = null;
    }

    @Override
    public List<Thing> filter(Collection<Thing> things) {
        return query.filter(things);
    }

    @Override
    public String toString() {
        return "JsonThingQuery{" +
                "sparql=" + query +
                '}';
    }

    public SparqlThingQuery getQuery() {
        return query;
    }

    static class JsonThingQueryQueryDeserializer extends JsonDeserializer {
        private static final Logger log = LoggerFactory.getLogger(JsonThingQueryQueryDeserializer.class);

        @Override
        public Object deserialize(JsonParser p,
                                  DeserializationContext ctxt) throws IOException {
            try {
                JsonToken t = p.currentToken();
                if (t == JsonToken.VALUE_STRING) {
                    return new SparqlThingQuery(jsonToSparqlQuery(p.getValueAsString()));
                }
                else {
                    log.warn("Unable to deserialize JsonThingQuery of type '{}'", t);
                    return null;
                }
            }
            catch (ThingQueryException e) {
                throw new IOException(e);
            }
        }
    }
}
