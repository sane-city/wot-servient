package city.sane.wot;

import city.sane.wot.binding.ProtocolServerNotImplementedException;
import city.sane.wot.binding.akka.AkkaProtocolClientFactory;
import city.sane.wot.binding.akka.AkkaProtocolServer;
import city.sane.wot.binding.coap.CoapProtocolClientFactory;
import city.sane.wot.binding.coap.CoapProtocolServer;
import city.sane.wot.binding.http.HttpProtocolClientFactory;
import city.sane.wot.binding.http.HttpProtocolServer;
import city.sane.wot.binding.mqtt.MqttProtocolClientFactory;
import city.sane.wot.binding.mqtt.MqttProtocolServer;
import city.sane.wot.binding.websocket.WebsocketProtocolClientFactory;
import city.sane.wot.binding.websocket.WebsocketProtocolServer;
import city.sane.wot.thing.Context;
import city.sane.wot.thing.ExposedThing;
import city.sane.wot.thing.Thing;
import city.sane.wot.thing.action.ThingAction;
import city.sane.wot.thing.event.ThingEvent;
import city.sane.wot.thing.filter.DiscoveryMethod;
import city.sane.wot.thing.filter.SparqlThingQuery;
import city.sane.wot.thing.filter.ThingFilter;
import city.sane.wot.thing.filter.ThingQuery;
import city.sane.wot.thing.property.ThingProperty;
import city.sane.wot.thing.schema.IntegerSchema;
import city.sane.wot.thing.schema.ObjectSchema;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import org.hamcrest.MatcherAssert;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.ArgumentsProvider;
import org.junit.jupiter.params.provider.ArgumentsSource;

import java.net.URI;
import java.util.Collection;
import java.util.Date;
import java.util.Map;
import java.util.stream.Stream;

import static org.hamcrest.Matchers.hasKey;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ServientIT {
    private Servient servient;

    @AfterEach
    public void teardown() {
        servient.shutdown().join();
    }

    @ParameterizedTest
    @ArgumentsSource(MyArgumentsProvider.class)
    public void destroy(Class server, Class clientFactory) throws ServientException {
        Config config = ConfigFactory
                .parseString("wot.servient.servers = [\"" + server.getName() + "\"]\n" +
                        "wot.servient.client-factories = [\"" + clientFactory.getName() + "\"]")
                .withFallback(ConfigFactory.load());
        servient = new Servient(config);
        servient.start().join();

        ExposedThing thing = getExposedCounterThing();
        servient.addThing(thing);
        thing.expose().join();
        thing.destroy().join();

        assertTrue(thing.getProperty("count").getForms().isEmpty(), "There must be no forms");
        assertTrue(thing.getAction("increment").getForms().isEmpty(), "There must be no actions");
        assertTrue(thing.getEvent("change").getForms().isEmpty(), "There must be no events");
    }

    private ExposedThing getExposedCounterThing() {
        ThingProperty counterProperty = new ThingProperty.Builder()
                .setType("integer")
                .setDescription("current counter content")
                .setObservable(true)
                .build();

        ThingProperty lastChangeProperty = new ThingProperty.Builder()
                .setType("string")
                .setDescription("last change of counter content")
                .setObservable(true)
                .setReadOnly(true)
                .build();

        ExposedThing thing = new ExposedThing(servient)
                .setId("counter")
                .setTitle("counter");

        thing.addProperty("count", counterProperty, 42);
        thing.addProperty("lastChange", lastChangeProperty, new Date().toString());

        thing.addAction("increment",
                new ThingAction.Builder()
                        .setDescription("Incrementing counter content with optional step content as uriVariable")
                        .setUriVariables(Map.of(
                                "step", Map.of(
                                        "type", "integer",
                                        "minimum", 1,
                                        "maximum", 250
                                )
                        ))
                        .setInput(new ObjectSchema())
                        .setOutput(new IntegerSchema())
                        .build(),
                (input, options) -> {
                    return thing.getProperty("count").read().thenApply(value -> {
                        int step;
                        if (input != null && ((Map) input).containsKey("step")) {
                            step = (Integer) ((Map) input).get("step");
                        }
                        else if (options.containsKey("uriVariables") && options.get("uriVariables").containsKey("step")) {
                            step = (int) options.get("uriVariables").get("step");
                        }
                        else {
                            step = 1;
                        }
                        int newValue = ((Integer) value) + step;
                        thing.getProperty("count").write(newValue);
                        thing.getProperty("lastChange").write(new Date().toString());
                        thing.getEvent("change").emit();
                        return newValue;
                    });
                });

        thing.addAction("decrement", new ThingAction<Object, Object>(), (input, options) -> {
            return thing.getProperty("count").read().thenApply(value -> {
                int newValue = ((Integer) value) - 1;
                thing.getProperty("count").write(newValue);
                thing.getProperty("lastChange").write(new Date().toString());
                thing.getEvent("change").emit();
                return newValue;
            });
        });

        thing.addAction("reset", new ThingAction<Object, Object>(), (input, options) -> {
            return thing.getProperty("count").write(0).thenApply(value -> {
                thing.getProperty("lastChange").write(new Date().toString());
                thing.getEvent("change").emit();
                return 0;
            });
        });

        thing.addEvent("change", new ThingEvent<Object>());

        return thing;
    }

    @ParameterizedTest
    @ArgumentsSource(MyArgumentsProvider.class)
    public void fetch(Class server, Class clientFactory) throws ServientException {
        Config config = ConfigFactory
                .parseString("wot.servient.servers = [\"" + server.getName() + "\"]\n" +
                        "wot.servient.client-factories = [\"" + clientFactory.getName() + "\"]")
                .withFallback(ConfigFactory.load());
        servient = new Servient(config);
        servient.start().join();

        try {
            ExposedThing exposedThing = getExposedCounterThing();
            servient.addThing(exposedThing);
            exposedThing.expose().join();

            URI url = servient.getServer(server).getThingUrl(exposedThing.getId());

            Thing thing = servient.fetch(url).join();

            assertEquals("counter", thing.getId());
        }
        catch (ProtocolServerNotImplementedException e) {

        }
    }

    @ParameterizedTest
    @ArgumentsSource(MyArgumentsProvider.class)
    public void fetchDirectory(Class server, Class clientFactory) throws ServientException {
        Config config = ConfigFactory
                .parseString("wot.servient.servers = [\"" + server.getName() + "\"]\n" +
                        "wot.servient.client-factories = [\"" + clientFactory.getName() + "\"]")
                .withFallback(ConfigFactory.load());
        servient = new Servient(config);
        servient.start().join();

        try {
            ExposedThing exposedThing = getExposedCounterThing();
            servient.addThing(exposedThing);
            exposedThing.expose().join();

            URI url = servient.getServer(server).getDirectoryUrl();

            Map things = servient.fetchDirectory(url).join();

            MatcherAssert.assertThat((Map<String, Thing>) things, hasKey("counter"));
        }
        catch (ProtocolServerNotImplementedException e) {

        }
    }

    @ParameterizedTest
    @ArgumentsSource(MyArgumentsProvider.class)
    public void discoverLocal(Class server, Class clientFactory) throws ServientException {
        Config config = ConfigFactory
                .parseString("wot.servient.servers = [\"" + server.getName() + "\"]\n" +
                        "wot.servient.client-factories = [\"" + clientFactory.getName() + "\"]")
                .withFallback(ConfigFactory.load());
        servient = new Servient(config);
        servient.start().join();

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
        ThingFilter filter = new ThingFilter(DiscoveryMethod.LOCAL);

        Collection<Thing> things = servient.discover(filter).toList().blockingGet();
        assertEquals(3, things.size());
    }

    @ParameterizedTest
    @ArgumentsSource(MyArgumentsProvider.class)
    public void discoverLocalWithQuery(Class server, Class clientFactory) throws ServientException {
        Config config = ConfigFactory
                .parseString("wot.servient.servers = [\"" + server.getName() + "\"]\n" +
                        "wot.servient.client-factories = [\"" + clientFactory.getName() + "\"]")
                .withFallback(ConfigFactory.load());
        servient = new Servient(config);
        servient.start().join();

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

        Collection<Thing> things = servient.discover(filter).toList().blockingGet();
        assertEquals(1, things.size());
    }

    private static class MyArgumentsProvider implements ArgumentsProvider {
        public MyArgumentsProvider() {
        }

        @Override
        public Stream<? extends Arguments> provideArguments(
                ExtensionContext extensionContext) {
            return Stream.of(
                    Arguments.of(AkkaProtocolServer.class, AkkaProtocolClientFactory.class),
                    Arguments.of(CoapProtocolServer.class, CoapProtocolClientFactory.class),
                    Arguments.of(HttpProtocolServer.class, HttpProtocolClientFactory.class),
                    // Jadex platform discovery is unstable
//                Arguments.of(JadexProtocolServer.class, JadexProtocolClientFactory.class),
                    Arguments.of(MqttProtocolServer.class, MqttProtocolClientFactory.class),
                    Arguments.of(WebsocketProtocolServer.class, WebsocketProtocolClientFactory.class)
            );
        }
    }
}
