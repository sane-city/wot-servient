package city.sane.wot.binding.jadex;

import jadex.commons.future.IFuture;

/**
 * Defines the Jadex Service interface for interaction with a Thing.
 */
public interface ThingService {
    /**
     * Return Thing as string because Jadex is not able to (de)serialize it properly without
     * additional adjustments.
     *
     * @return
     */
    IFuture<String> get();

    IFuture<JadexContent> readProperties();

    IFuture<JadexContent> readProperty(String name);

    IFuture<JadexContent> writeProperty(String name, JadexContent content);

    String getThingServiceId();
}
