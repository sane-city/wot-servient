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

import city.sane.wot.thing.Thing;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.rio.RDFFormat;
import org.eclipse.rdf4j.rio.Rio;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.StringReader;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

/**
 * Allows filtering of things during discovery process using a JSON query.
 * <p>
 * Example Query: {"@type":"https://www.w3.org/2019/wot/td#Thing"}
 * </p>
 */
public class JsonThingQuery implements ThingQuery {
    private final String query;

    public JsonThingQuery(String query) {
        this.query = query;
    }

    JsonThingQuery() {
        // required by jackson
        query = null;
    }

    @Override
    public int hashCode() {
        return Objects.hash(query);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        JsonThingQuery that = (JsonThingQuery) o;
        return Objects.equals(query, that.query);
    }

    @Override
    public String toString() {
        return "JsonThingQuery{" +
                "sparql=" + query +
                '}';
    }

    @Override
    public List<Thing> filter(Collection<Thing> things) throws ThingQueryException {
        return getSparqlQuery().filter(things);
    }

    SparqlThingQuery getSparqlQuery() throws ThingQueryException {
        try {
            StringReader reader = new StringReader(query);
            Model frame = Rio.parse(reader, "", RDFFormat.JSONLD);
            ByteArrayOutputStream o = new ByteArrayOutputStream();
            Rio.write(frame, o, RDFFormat.NTRIPLES);

            String sparlQueryString = o.toString().replace("_:", "?");

            if (sparlQueryString.isEmpty()) {
                throw new ThingQueryException("Translated SPARQL Query is empty. Something seems wrong with your JSON query: " + query);
            }

            return new SparqlThingQuery(sparlQueryString);
        }
        catch (IOException e) {
            throw new ThingQueryException(e);
        }
    }

    public String getQuery() {
        return query;
    }
}
