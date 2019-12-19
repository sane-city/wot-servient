package city.sane.wot;

import city.sane.Pair;
import city.sane.wot.binding.ProtocolClientFactory;
import city.sane.wot.binding.ProtocolServer;
import city.sane.wot.binding.ProtocolServerException;
import city.sane.wot.binding.ProtocolServerNotImplementedException;
import city.sane.wot.binding.akka.AkkaProtocolClientFactory;
import city.sane.wot.binding.akka.AkkaProtocolServer;
import city.sane.wot.binding.coap.CoapProtocolClientFactory;
import city.sane.wot.binding.coap.CoapProtocolServer;
import city.sane.wot.binding.http.HttpProtocolClientFactory;
import city.sane.wot.binding.http.HttpProtocolServer;
import city.sane.wot.binding.jadex.JadexProtocolClientFactory;
import city.sane.wot.binding.jadex.JadexProtocolServer;
import city.sane.wot.binding.mqtt.MqttProtocolClientFactory;
import city.sane.wot.binding.mqtt.MqttProtocolServer;
import city.sane.wot.binding.websocket.WebsocketProtocolClientFactory;
import city.sane.wot.binding.websocket.WebsocketProtocolServer;
import city.sane.wot.thing.ExposedThing;
import city.sane.wot.thing.Thing;
import city.sane.wot.thing.action.ThingAction;
import city.sane.wot.thing.event.ThingEvent;
import city.sane.wot.thing.property.ThingProperty;
import city.sane.wot.thing.schema.IntegerSchema;
import city.sane.wot.thing.schema.ObjectSchema;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import org.hamcrest.Matchers;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.net.URI;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

@RunWith(Parameterized.class)
public class ServientIT {
    @Parameterized.Parameter
    public Pair<Class<? extends ProtocolServer>, Class<? extends ProtocolClientFactory>> servientClasses;
    private Servient servient;

    @Before
    public void setup() throws ServientException {
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
    public void fetch() throws ProtocolServerException {
        try {
            ExposedThing exposedThing = getExposedCounterThing();
            servient.addThing(exposedThing);
            exposedThing.expose().join();

            URI url = servient.getServer(servientClasses.first()).getThingUrl(exposedThing.getId());

            Thing thing = servient.fetch(url).join();

            assertEquals("counter", thing.getId());
        }
        catch (ProtocolServerNotImplementedException e) {

        }
    }

    @Test
    public void fetchDirectory() throws ProtocolServerException {
        try {
            ExposedThing exposedThing = getExposedCounterThing();
            servient.addThing(exposedThing);
            exposedThing.expose().join();

            URI url = servient.getServer(servientClasses.first()).getDirectoryUrl();

            Map things = servient.fetchDirectory(url).join();

            assertThat((Map<String, Thing>) things, Matchers.hasKey("counter"));
        }
        catch (ProtocolServerNotImplementedException e) {

        }
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
                        else if (options.containsKey("uriVariables") && ((Map) options.get("uriVariables")).containsKey("step")) {
                            step = (int) ((Map) options.get("uriVariables")).get("step");
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

        thing.addAction("decrement", new ThingAction(), (input, options) -> {
            return thing.getProperty("count").read().thenApply(value -> {
                int newValue = ((Integer) value) - 1;
                thing.getProperty("count").write(newValue);
                thing.getProperty("lastChange").write(new Date().toString());
                thing.getEvent("change").emit();
                return newValue;
            });
        });

        thing.addAction("reset", new ThingAction(), (input, options) -> {
            return thing.getProperty("count").write(0).thenApply(value -> {
                thing.getProperty("lastChange").write(new Date().toString());
                thing.getEvent("change").emit();
                return 0;
            });
        });

        thing.addEvent("change", new ThingEvent());

        return thing;
    }

    @Parameterized.Parameters(name = "{0}")
    public static Collection<Pair<Class<? extends ProtocolServer>, Class<? extends ProtocolClientFactory>>> data() {
        return Arrays.asList(
                // akka-binding with relay is not fully supported by now
//                new Pair<>(AkkaProtocolServer.class, AkkaProtocolClientFactory.class),
                new Pair<>(CoapProtocolServer.class, CoapProtocolClientFactory.class),
                new Pair<>(HttpProtocolServer.class, HttpProtocolClientFactory.class),
                new Pair<>(JadexProtocolServer.class, JadexProtocolClientFactory.class),
                new Pair<>(MqttProtocolServer.class, MqttProtocolClientFactory.class),
                new Pair<>(WebsocketProtocolServer.class, WebsocketProtocolClientFactory.class)
        );
    }
}
