package city.sane.wot.thing;

import city.sane.Pair;
import city.sane.wot.Servient;
import city.sane.wot.ServientException;
import city.sane.wot.binding.ProtocolClientFactory;
import city.sane.wot.binding.ProtocolServer;
import city.sane.wot.binding.jadex.JadexProtocolClientFactory;
import city.sane.wot.binding.jadex.JadexProtocolServer;
import city.sane.wot.thing.action.ConsumedThingAction;
import city.sane.wot.thing.action.ThingAction;
import city.sane.wot.thing.event.ThingEvent;
import city.sane.wot.thing.observer.Subscription;
import city.sane.wot.thing.property.ConsumedThingProperty;
import city.sane.wot.thing.property.ThingProperty;
import city.sane.wot.thing.schema.IntegerSchema;
import city.sane.wot.thing.schema.ObjectSchema;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameter;
import org.junit.runners.Parameterized.Parameters;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.Assert.assertEquals;

@Ignore("Jadex platform discovery is unstable")
@RunWith(Parameterized.class)
public class ConsumedThingTest {
    @Parameter
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

    @Test(timeout = 20 * 1000)
    public void observeProperty() throws ConsumedThingException, InterruptedException, ExecutionException {
        ExposedThing exposedThing = getExposedCounterThing();
        servient.addThing(exposedThing);
        exposedThing.expose().join();

        ConsumedThing consumedThing = new ConsumedThing(servient, exposedThing);

        AtomicInteger counter1 = new AtomicInteger();
        Subscription subscription1 = consumedThing.getProperty("count").subscribe(
                next -> counter1.getAndIncrement()
        ).get();

        AtomicInteger counter2 = new AtomicInteger();
        Subscription subscription2 = consumedThing.getProperty("count").subscribe(
                next -> counter2.getAndIncrement()
        ).get();

        // wait until client establish subcription
        // TODO: This is error-prone. We need a feature that notifies us when the subscription is active.
        Thread.sleep(5 * 1000L);

        exposedThing.getProperty("count").write(1337).join();

        // wait until client fires next subscribe-request to server
        // TODO: This is error-prone. We need a feature that notifies us when the subscription is active.
        Thread.sleep(5 * 1000L);

        exposedThing.getProperty("count").write(1338).join();

        // Subscriptions are executed asynchronously. Therefore, wait "some" time before we check the result.
        // TODO: This is error-prone. We need a function that notifies us when all subscriptions have been executed.
        Thread.sleep(5 * 1000L);

        subscription1.unsubscribe();
        subscription2.unsubscribe();

        assertEquals(2, counter1.get());
        assertEquals(2, counter2.get());
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

    @Test
    public void invokeAction() throws ExecutionException, InterruptedException {
        ExposedThing exposedThing = getExposedCounterThing();
        servient.addThing(exposedThing);
        exposedThing.expose().join();

        ConsumedThing thing = new ConsumedThing(servient, exposedThing);

        ConsumedThingAction increment = thing.getAction("increment");

        CompletableFuture future = increment.invoke(Map.of("step", 3));
        assertEquals(45, future.get());
    }

    @Test(timeout = 20 * 1000)
    public void emitEvent() throws ConsumedThingException, InterruptedException, ExecutionException {
        ExposedThing exposedThing = getExposedCounterThing();
        servient.addThing(exposedThing);
        exposedThing.expose().join();

        ConsumedThing consumedThing = new ConsumedThing(servient, exposedThing);

        AtomicInteger counter1 = new AtomicInteger();
        Subscription subscription1 = consumedThing.getEvent("change").subscribe(
                next -> counter1.getAndIncrement()
        ).get();

        AtomicInteger counter2 = new AtomicInteger();
        Subscription subscription2 = consumedThing.getEvent("change").subscribe(
                next -> counter2.getAndIncrement()
        ).get();

        // wait until client establish subcription
        // TODO: This is error-prone. We need a feature that notifies us when the subscription is active.
        Thread.sleep(5 * 1000L);

        exposedThing.getEvent("change").emit();

        // wait until client fires next subscribe-request to server
        // TODO: This is error-prone. We need a feature that notifies us when the subscription is active.
        Thread.sleep(5 * 1000L);

        exposedThing.getEvent("change").emit();

        // Subscriptions are executed asynchronously. Therefore, wait "some" time before we check the result.
        // TODO: This is error-prone. We need a function that notifies us when all subscriptions have been executed.
        Thread.sleep(5 * 1000L);

        subscription1.unsubscribe();
        subscription2.unsubscribe();

        assertEquals(2, counter1.get());
        assertEquals(2, counter2.get());
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

    @Parameters(name = "{0}")
    public static Collection<Pair<Class<? extends ProtocolServer>, Class<? extends ProtocolClientFactory>>> data() {
        return Collections.singletonList(
                new Pair<>(JadexProtocolServer.class, JadexProtocolClientFactory.class)
        );
    }
}
