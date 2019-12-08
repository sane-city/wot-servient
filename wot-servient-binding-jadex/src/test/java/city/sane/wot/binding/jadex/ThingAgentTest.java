package city.sane.wot.binding.jadex;

import city.sane.wot.Servient;
import city.sane.wot.ServientException;
import city.sane.wot.thing.ConsumedThing;
import city.sane.wot.thing.ExposedThing;
import city.sane.wot.thing.action.ThingAction;
import city.sane.wot.thing.event.ThingEvent;
import city.sane.wot.thing.property.ConsumedThingProperty;
import city.sane.wot.thing.property.ThingProperty;
import city.sane.wot.thing.schema.IntegerSchema;
import city.sane.wot.thing.schema.ObjectSchema;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.Collections;
import java.util.Date;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import static org.junit.Assert.assertEquals;

public class ThingAgentTest {
    private Servient servient;

    @Before
    public void setup() throws ServientException {
        Config config = ConfigFactory
                .parseString("wot.servient.servers = [\"" + JadexProtocolServer.class.getName() + "\"]\n" +
                        "wot.servient.client-factories = [\"" + JadexProtocolClientFactory.class.getName() + "\"]")
                .withFallback(ConfigFactory.load());

        servient = new Servient(config);
        servient.start().join();
    }

    @After
    public void teardown() {
        servient.shutdown().join();
    }

    @Test
    public void readProperty() throws ExecutionException, InterruptedException {
        ExposedThing exposedThing = getExposedCounterThing();
        servient.addThing(exposedThing);
        exposedThing.expose().join();

        ConsumedThing thing = new ConsumedThing(servient, exposedThing);

        ConsumedThingProperty counter = thing.getProperty("count");
        assertEquals(42, counter.read().get());
    }

    @Test
    public void writeProperty() throws ExecutionException, InterruptedException {
        ExposedThing exposedThing = getExposedCounterThing();
        servient.addThing(exposedThing);
        exposedThing.expose().join();

        ConsumedThing thing = new ConsumedThing(servient, exposedThing);

        ConsumedThingProperty counter = thing.getProperty("count");
        counter.write(1337).join();
        assertEquals(1337, counter.read().get());
    }

    @Test
    public void readProperties() throws ExecutionException, InterruptedException {
        ExposedThing exposedThing = getExposedCounterThing();
        servient.addThing(exposedThing);
        exposedThing.expose().join();

        ConsumedThing thing = new ConsumedThing(servient, exposedThing);

        Map values = thing.readProperties().get();
        assertEquals(2, values.size());
        assertEquals(42, values.get("count"));
    }

    @Test
    public void readMultipleProperties() throws ExecutionException, InterruptedException {
        ExposedThing exposedThing = getExposedCounterThing();
        servient.addThing(exposedThing);
        exposedThing.expose().join();

        ConsumedThing thing = new ConsumedThing(servient, exposedThing);

        Map values = thing.readProperties(Collections.singletonList("count")).get();
        assertEquals(1, values.size());
        assertEquals(42, values.get("count"));
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
}