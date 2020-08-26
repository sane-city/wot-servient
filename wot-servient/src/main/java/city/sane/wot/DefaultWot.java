package city.sane.wot;

import city.sane.wot.thing.ConsumedThing;
import city.sane.wot.thing.ExposedThing;
import city.sane.wot.thing.Thing;
import city.sane.wot.thing.filter.DiscoveryMethod;
import city.sane.wot.thing.filter.ThingFilter;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import io.reactivex.rxjava3.core.Observable;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.concurrent.CompletableFuture;

/**
 * Standard implementation of {@link Wot}.
 */
public class DefaultWot implements Wot {
    protected final Servient servient;

    /**
     * Creates a new DefaultWot instance for given <code>servient</code>.
     *
     * @param servient
     */
    public DefaultWot(Servient servient) {
        this.servient = servient;
    }

    /**
     * Creates and starts a {@link Servient}.
     */
    public DefaultWot() throws WotException {
        this(ConfigFactory.load());
    }

    /**
     * Creates and starts a {@link Servient} with the given <code>config</code>.
     *
     * @param config
     */
    public DefaultWot(Config config) throws WotException {
        servient = new Servient(config);
        servient.start().join();
    }

    @Override
    public String toString() {
        return "DefaultWot{" +
                "servient=" + servient +
                '}';
    }

    /**
     * Creates and starts a {@link Servient}. The servient will not start any servers and can
     * therefore only consume things and not expose any things.
     */
    public static Wot clientOnly() throws WotException {
        return clientOnly(ConfigFactory.load());
    }

    /**
     * Creates and starts a {@link Servient} with the given <code>config</code>. The servient will
     * not start any servers and can therefore only consume things and not expose any things.
     *
     * @param config
     */
    public static Wot clientOnly(Config config) throws WotException {
        Servient servient = Servient.clientOnly(config);
        servient.start().join();
        return new DefaultWot(servient);
    }

    /**
     * Creates and starts a {@link Servient}. The servient will not start any clients and can
     * therefore only produce and expose things.
     */
    public static Wot serverOnly() throws WotException {
        return serverOnly(ConfigFactory.load());
    }

    /**
     * Creates and starts a {@link Servient} with the given <code>config</code>. The servient will
     * not start any clients and can therefore only produce and expose things.
     *
     * @param config
     */
    public static Wot serverOnly(Config config) throws WotException {
        Servient servient = Servient.serverOnly(config);
        servient.start().join();
        return new DefaultWot(servient);
    }

    @Override
    public Observable<Thing> discover(ThingFilter filter) throws WotException {
        return servient.discover(filter);
    }

    @Override
    public Observable<Thing> discover() throws WotException {
        return discover(new ThingFilter(DiscoveryMethod.ANY));
    }

    @Override
    public ExposedThing produce(Thing thing) throws WotException {
        ExposedThing exposedThing = new ExposedThing(servient, thing);
        if (servient.addThing(exposedThing)) {
            return exposedThing;
        }
        else {
            throw new WotException("Thing already exists: " + thing.getId());
        }
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
}
