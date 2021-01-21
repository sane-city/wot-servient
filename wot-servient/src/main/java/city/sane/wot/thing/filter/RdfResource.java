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

import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.impl.LinkedHashModel;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.eclipse.rdf4j.model.vocabulary.DCAT;
import org.eclipse.rdf4j.model.vocabulary.DCTERMS;
import org.eclipse.rdf4j.model.vocabulary.RDF;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Date;
import java.util.UUID;

/**
 * Implementation of the org.eclipse.thingweb.directory.Resource interface as an RDF dataset, where
 * the actual content of a resource is stored in an RDF graph.
 * <p>
 * In this implementation, directory resources are interpreted as DCAT datasets.
 * <p>
 * Adapted from <a href="https://github.com/thingweb/thingweb-directory/blob/2ac64745dac1e0070bfe55b8d14b19614d205e81/directory-core/src/main/groovy/org/eclipse/thingweb/directory/rdf/RDFResource.groovy">https://github.com/thingweb/thingweb-directory</a>
 *
 * @author Victor Charpenay
 * @see <a href="https://www.w3.org/TR/vocab-dcat/">
 * DCAT RDF vocabulary
 * </a>
 */
class RdfResource {
    private static final Logger log = LoggerFactory.getLogger(RdfResource.class);
    /**
     * DCAT meta-data
     */
    private final Model metadata = new LinkedHashModel();
    /**
     * Underlying RDF graph
     */
    private final Model content = new LinkedHashModel();
    private final Resource iri;

    public RdfResource(Model content) {
        if (content.contexts().size() > 1) {
            log.warn("Named graphs in RDF resource content ignored");
        }
        // TODO check if content is empty (otherwise, the manager will always return 'not found')
        this.content.addAll(content);

        this.iri = generate(content);

        this.metadata.add(iri, RDF.TYPE, DCAT.DATASET);
        this.metadata.add(iri, DCTERMS.ISSUED, SimpleValueFactory.getInstance().createLiteral(new Date()));

        log.debug("Creating RDF resource object with id <{}>", iri);
    }

    /**
     * Generates a default IRI for the given RDF graph
     *
     * @param g an RDF graph
     * @return a UUID URN
     */
    @SuppressWarnings({ "java:S1172" })
    private static IRI generate(Model g) {
        // TODO normalize graph and always return the same id for a fixed graph
        return SimpleValueFactory.getInstance().createIRI("urn:uuid:" + UUID.randomUUID());
    }

    public Model getMetadata() {
        return metadata;
    }

    public Model getContent() {
        return content;
    }

    public Resource getIri() {
        return iri;
    }
}
