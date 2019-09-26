package city.sane.wot.thing.filter;

/**
 * Defines "Where" to search for things during a discovery process.
 */
public enum DiscoveryMethod {
    /**
     * Uses the discovery mechanisms provided by all {@link city.sane.wot.binding.ProtocolClient} implementations to consider all available Things.
     */
    ANY,

    /**
     * Searches only on the local {@link city.sane.wot.Servient}.
     */
    LOCAL,

    /**
     * Is used together with a URL to search in a specific Thing Directory.
     */
    DIRECTORY,

//    MULTICAST
}
