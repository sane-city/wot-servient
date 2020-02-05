package city.sane.wot.thing.filter;

import city.sane.wot.thing.Thing;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.rio.RDFFormat;
import org.eclipse.rdf4j.rio.Rio;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.StringReader;
import java.util.Collection;
import java.util.List;

/**
 * Allows filtering of things during discovery process using a JSON query.
 * <p>
 * Example Query:
 * {"@type":"https://www.w3.org/2019/wot/td#Thing"}
 */
public class JsonThingQuery implements ThingQuery {
    private final SparqlThingQuery sparql;

    public JsonThingQuery(String query) throws ThingQueryException {
        try {
            StringReader reader = new StringReader(query);
            Model frame = Rio.parse(reader, "", RDFFormat.JSONLD);
            ByteArrayOutputStream o = new ByteArrayOutputStream();
            Rio.write(frame, o, RDFFormat.NTRIPLES);

            String sparqlQuery = o.toString().replace("_:", "?");

            sparql = new SparqlThingQuery(sparqlQuery);
        }
        catch (IOException e) {
            throw new ThingQueryException(e);
        }
    }

    @Override
    public List<Thing> filter(Collection<Thing> things) {
        return sparql.filter(things);
    }

    @Override
    public String toString() {
        return "JsonThingQuery{" +
                "sparql=" + sparql +
                '}';
    }
}
