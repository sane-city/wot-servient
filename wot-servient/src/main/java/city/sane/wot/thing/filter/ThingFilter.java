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

import java.net.URI;
import java.util.Objects;

/**
 * ThingFilter is used for the discovery process and specifies what things to look for and where to
 * look for them.
 */
public class ThingFilter {
    private DiscoveryMethod method;
    private URI url;
    private ThingQuery query;

    public ThingFilter() {
        this(DiscoveryMethod.ANY);
    }

    public ThingFilter(DiscoveryMethod method) {
        this.method = method;
    }

    public DiscoveryMethod getMethod() {
        return method;
    }

    /**
     * Specifies where to search for Things. {@link DiscoveryMethod#LOCAL} searches only on the
     * local {@link city.sane.wot.Servient}, {@link DiscoveryMethod#DIRECTORY} searches in the Thing
     * Directory defined in {@link #url}. {@link DiscoveryMethod#ANY} uses the discovery mechanisms
     * provided by all {@link city.sane.wot.binding.ProtocolClient} implementations to consider all
     * available Things.
     *
     * @param method
     * @return
     */
    public ThingFilter setMethod(DiscoveryMethod method) {
        this.method = method;
        return this;
    }

    public URI getUrl() {
        return url;
    }

    /**
     * Used in combination with {@link DiscoveryMethod#DIRECTORY} and defines the URL of the Thing
     * Directory to be searched in.
     *
     * @param url
     * @return
     */
    public ThingFilter setUrl(URI url) {
        this.url = url;
        return this;
    }

    public ThingQuery getQuery() {
        return query;
    }

    /**
     * Defines a query that filters the Things found according to certain properties.<br> See also
     * {@link SparqlThingQuery} and {@link JsonThingQuery}
     *
     * @param query
     * @return
     */
    public ThingFilter setQuery(ThingQuery query) {
        this.query = query;
        return this;
    }

    @Override
    public int hashCode() {
        return Objects.hash(method, url, query);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ThingFilter that = (ThingFilter) o;
        return method == that.method &&
                Objects.equals(url, that.url) &&
                Objects.equals(query, that.query);
    }

    @Override
    public String toString() {
        return "ThingFilter{" +
                "method=" + method +
                ", url=" + url +
                ", query=" + query +
                '}';
    }
}
