package city.sane.wot.thing.filter;

import city.sane.wot.thing.Thing;
import org.eclipse.rdf4j.common.iteration.Iterations;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.query.BindingSet;
import org.eclipse.rdf4j.repository.sail.SailRepository;
import org.eclipse.rdf4j.repository.util.Repositories;
import org.eclipse.rdf4j.rio.RDFFormat;
import org.eclipse.rdf4j.rio.Rio;
import org.eclipse.rdf4j.sail.memory.MemoryStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.StringReader;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Allows filtering of things discovery process using a SPARQL query.
 * <p>
 * Example Query:
 * ?x &lt;http://www.w3.org/1999/02/22-rdf-syntax-ns#type&gt; &lt;https://www.w3.org/2019/wot/td#Thing&gt; .
 */
public class SparqlThingQuery implements ThingQuery {
    private static final Logger log = LoggerFactory.getLogger(SparqlThingQuery.class);

    // FIXME use tag: or urn: instead (see https://github.com/jsonld-java/jsonld-java/issues/232)
    private static final String DEFAULT_BASE_IRI = "https://sane.city/";
    private static final RDFFormat FORMAT = Rio.getParserFormatForMIMEType("application/td+json").orElse(RDFFormat.JSONLD);

    private final String query;

    public SparqlThingQuery(String query) throws ThingQueryException {
        if (query.matches(".*\\?__id__\\s.*")) {
            throw new ThingQueryException("Your query is invalid because it contains the reserved variable '?__id__'");
        }
        this.query = query;
    }

    @Override
    public String toString() {
        return "SparqlThingQuery{" +
                "query='" + query + '\'' +
                '}';
    }

    @Override
    public List<Thing> filter(Collection<Thing> things) {
        // create rdf repository with all things
        SailRepository repository = new SailRepository(new MemoryStore());
        repository.init();

        Map<String, Thing> resourceToThings = new HashMap<>();
        for (Thing thing : things) {
            StringReader reader = new StringReader(thing.toJson());
            try {
                Model model = Rio.parse(reader, DEFAULT_BASE_IRI, FORMAT);
                RdfResource resource = new RdfResource(model);

                resourceToThings.put(resource.getIri().stringValue(), thing);

                Repositories.consume(repository, connection -> {
                    connection.add(resource.getMetadata());
                    connection.add(resource.getContent(), resource.getIri());
                });
            }
            catch (IOException e) {
                log.warn("Unable to create rdf resource for thing {}: {}", thing.getId(), e.getMessage());
            }
        }

        // apply query on repository
        Set<String> filteredIris = Repositories.tupleQuery(repository, "SELECT DISTINCT ?__id__ WHERE { GRAPH ?__id__ { " + query + " }}", result -> {
            Set<BindingSet> bindings = Iterations.asSet(result);
            return bindings.stream().map(b -> b.getValue("__id__").stringValue()).collect(Collectors.toSet());
        });

        // map returned iris to things
        List<Thing> filteredThings = filteredIris.stream()
                .map(resourceToThings::get).collect(Collectors.toList());

        repository.shutDown();

        return filteredThings;
    }
}
