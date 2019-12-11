package city.sane.wot;

import city.sane.wot.thing.ConsumedThing;
import city.sane.wot.thing.ExposedThing;
import city.sane.wot.thing.Thing;
import city.sane.wot.thing.filter.DiscoveryMethod;
import city.sane.wot.thing.filter.ThingFilter;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collection;
import java.util.concurrent.CompletableFuture;

/**
 * Standard implementation of {@link Wot}.
 */
public class DefaultWot implements Wot {
    private final Servient servient;

    /**
     * Creates a new DefaultWot instance for given <code>servient</code>.
     *
     * @param servient
     */
    public DefaultWot(Servient servient) {
        this.servient = servient;
    }

    /**
     * Creates and starts a {@link Servient} with the given <code>config</code>.
     *
     * @param config
     */
    private DefaultWot(Config config) throws WotException {
        try {
            servient = new Servient(config);
            servient.start().join();
        }
        catch (ServientException e) {
            throw new WotException(e);
        }
    }

    /**
     * Creates and starts a {@link Servient}.
     */
    public DefaultWot() throws WotException {
        this(ConfigFactory.load());
    }

    @Override
    public CompletableFuture<Collection<Thing>> discover(ThingFilter filter) {
        return servient.discover(filter);
    }

    @Override
    public CompletableFuture<Collection<Thing>> discover() {
        return discover(new ThingFilter(DiscoveryMethod.ANY));
    }

    @Override
    public ExposedThing produce(Thing thing) {
        ExposedThing exposedThing = new ExposedThing(servient, thing);
        servient.addThing(exposedThing);
        return exposedThing;
    }

    @Override
    public ConsumedThing consume(Thing thing) {
        return new ConsumedThing(servient, thing);
    }

    @Override
    public ConsumedThing consume(String thing) {
        return consume(Thing.fromJson(thing));
    }

    @Override
    public CompletableFuture<Thing> fetch(URI url) {
        return servient.fetch(url);
    }

    @Override
    public CompletableFuture<Thing> fetch(String url) throws URISyntaxException {
        return servient.fetch(url);
    }

    @Override
    public CompletableFuture<Void> destroy() {
        return servient.shutdown();
    }

    /**
     * Creates and starts a {@link Servient}. The servient will not start any servers and can therefore only consume things
     * and not expose any things.
     */
    public static Wot clientOnly() throws WotException {
        return clientOnly(ConfigFactory.load());
    }

    /**
     * Creates and starts a {@link Servient} with the given <code>config</code>. The servient will not start any servers and can therefore only consume things
     * and not expose any things.
     *
     * @param config
     */
    public static Wot clientOnly(Config config) throws WotException {
        try {
            Servient servient = Servient.clientOnly(config);
            servient.start().join();
            return new DefaultWot(servient);
        }
        catch (ServientException e) {
            throw new WotException(e);
        }
    }
}
