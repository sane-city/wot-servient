package city.sane.wot;

import city.sane.wot.binding.ProtocolClientNotImplementedException;
import city.sane.wot.thing.ConsumedThing;
import city.sane.wot.thing.ExposedThing;
import city.sane.wot.thing.Thing;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import org.junit.Test;

import java.util.concurrent.ExecutionException;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertNull;

public class DefaultWotTest {
    @Test(expected = ProtocolClientNotImplementedException.class)
    public void discoverNoClients() throws Throwable {
        DefaultWot wot = new DefaultWot();

        try {
            wot.discover().get();
        }
        catch (ExecutionException e) {
            throw e.getCause();
        }
    }

    @Test
    public void produce() throws WotException {
        DefaultWot wot = new DefaultWot();

        Thing thing = new Thing.Builder().build();
        assertThat(wot.produce(thing), instanceOf(ExposedThing.class));
    }

    @Test
    public void consume() throws WotException {
        DefaultWot wot = new DefaultWot();

        Thing thing = new Thing.Builder().build();
        assertThat(wot.consume(thing), instanceOf(ConsumedThing.class));
    }

    @Test(expected = ServientException.class)
    public void fetchUnknownScheme() throws Throwable {
        DefaultWot wot = new DefaultWot();

        try {
            String url = "http://localhost";
            wot.fetch(url).get();
        }
        catch (ExecutionException e) {
            throw e.getCause();
        }
    }

    @Test
    public void destroy() throws WotException, ExecutionException, InterruptedException {
        DefaultWot wot = new DefaultWot();

        assertNull(wot.destroy().get());
    }

    @Test
    public void clientOnly() throws WotException {
        Wot wot = DefaultWot.clientOnly();

        assertThat(wot, instanceOf(Wot.class));
    }

    @Test(expected = WotException.class)
    public void clientOnlyBadServient() throws WotException {
        Config config = ConfigFactory.parseString("wot.servient.client-factories = [foo.bar.MyClientFactory]").withFallback(ConfigFactory.load());
        DefaultWot.clientOnly(config);
    }
}