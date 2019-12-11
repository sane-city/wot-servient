package city.sane.wot.binding.jadex;

import jadex.bridge.IExternalAccess;
import jadex.commons.future.IFuture;

/**
 * Defines the Jadex Service interface for the exposing and no longer exposing of Things.
 */
interface ThingsService {
    IFuture<IExternalAccess> expose(String id);

    IFuture<Void> destroy(String id);
}
