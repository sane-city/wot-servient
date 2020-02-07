package city.sane.wot.thing.filter;

import java.io.Serializable;
import java.net.URI;

/**
 * ThingFilter is used for the discovery process and specifies what things to look for and where to
 * look for them.
 */
public class ThingFilter implements Serializable {
    private DiscoveryMethod method;
    private URI url;
    private ThingQuery query;

    public ThingFilter() {
        this(DiscoveryMethod.ANY);
    }

    public ThingFilter(DiscoveryMethod method) {
        this.method = method;
    }

    @Override
    public String toString() {
        return "ThingFilter{" +
                "method=" + method +
                ", url=" + url +
                ", query=" + query +
                '}';
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
}
