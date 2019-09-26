package city.sane.wot.binding;

import city.sane.Pair;
import city.sane.wot.Servient;
import city.sane.wot.binding.jadex.JadexProtocolClientFactory;
import city.sane.wot.binding.jadex.JadexProtocolServer;
import city.sane.wot.thing.Context;
import city.sane.wot.thing.ExposedThing;
import city.sane.wot.thing.Thing;
import city.sane.wot.thing.filter.DiscoveryMethod;
import city.sane.wot.thing.filter.SparqlThingQuery;
import city.sane.wot.thing.filter.ThingFilter;
import city.sane.wot.thing.filter.ThingQuery;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.net.URI;
import java.util.Arrays;
import java.util.Collection;
import java.util.concurrent.ExecutionException;

import static org.junit.Assert.assertEquals;

@Ignore("Jadex platform discovery is unstable")
@RunWith(Parameterized.class)
public class ProtocolClientTest {
    @Parameterized.Parameter
    public Pair<Class<? extends ProtocolServer>, Class<? extends ProtocolClientFactory>> servientClasses;
    private Servient servient;

    @Before
    public void setup() {
        Config config = ConfigFactory
                .parseString("wot.servient.servers = [\"" + servientClasses.first().getName() + "\"]\n" +
                        "wot.servient.client-factories = [\"" + servientClasses.second().getName() + "\"]")
                .withFallback(ConfigFactory.load());

        servient = new Servient(config);
        servient.start().join();
    }

    @After
    public void teardown() {
        servient.shutdown().join();
    }

    @Test
    public void discoverWithDirectory() throws ExecutionException, InterruptedException, ProtocolServerException {
        // expose things so that something can be discovered
        ExposedThing thingX = new ExposedThing(servient).setId("ThingX");
        servient.addThing(thingX);
        thingX.expose().join();
        ExposedThing thingY = new ExposedThing(servient).setId("ThingY");
        servient.addThing(thingY);
        thingY.expose().join();
        ExposedThing thingZ = new ExposedThing(servient).setId("ThingZ");
        servient.addThing(thingZ);
        thingZ.expose().join();

        // discover
        URI url = servient.getServer(servientClasses.first()).getDirectoryUrl();
        ThingFilter filter = new ThingFilter(DiscoveryMethod.DIRECTORY).setUrl(url);

        Collection<Thing> things = servient.discover(filter).get();
        assertEquals(3, things.size());
    }

    @Test
    public void discoverAny() throws ExecutionException, InterruptedException {
        // expose things so that something can be discovered
        ExposedThing thingX = new ExposedThing(servient).setId("ThingX");
        servient.addThing(thingX);
        thingX.expose().join();
        ExposedThing thingY = new ExposedThing(servient).setId("ThingY");
        servient.addThing(thingY);
        thingY.expose().join();
        ExposedThing thingZ = new ExposedThing(servient).setId("ThingZ");
        servient.addThing(thingZ);
        thingZ.expose().join();

        // discover
        Collection<Thing> things = servient.discover().get();
        assertEquals(3, things.size());
    }

    @Test
    public void discoverAnyWithQuery() throws ExecutionException, InterruptedException {
        // expose things so that something can be discovered
        ExposedThing thingX = new ExposedThing(servient)
                .setId("ThingX")
                .setObjectContexts(new Context("https://www.w3.org/2019/wot/td/v1"))
                .setObjectType("Thing");
        servient.addThing(thingX);
        thingX.expose().join();
        ExposedThing thingY = new ExposedThing(servient).setId("ThingY");
        servient.addThing(thingY);
        thingY.expose().join();
        ExposedThing thingZ = new ExposedThing(servient).setId("ThingZ");
        servient.addThing(thingZ);
        thingZ.expose().join();

        // discover
        ThingFilter filter = new ThingFilter(DiscoveryMethod.LOCAL);
        ThingQuery sparqlQuery = new SparqlThingQuery("?x <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <https://www.w3.org/2019/wot/td#Thing> .");
        filter.setQuery(sparqlQuery);

        Collection<Thing> things = servient.discover(filter).get();
        assertEquals(1, things.size());
    }

    @Parameterized.Parameters(name = "{0}")
    public static Collection<Pair<Class<? extends ProtocolServer>, Class<? extends ProtocolClientFactory>>> data() {
        return Arrays.asList(
                new Pair<>(JadexProtocolServer.class, JadexProtocolClientFactory.class)
        );
    }
}

