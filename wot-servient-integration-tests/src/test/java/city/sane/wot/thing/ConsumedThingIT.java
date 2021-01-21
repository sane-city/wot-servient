package city.sane.wot.thing;

import city.sane.wot.Servient;
import city.sane.wot.ServientException;
import city.sane.wot.binding.ProtocolClientNotImplementedException;
import city.sane.wot.binding.akka.AkkaProtocolClientFactory;
import city.sane.wot.binding.akka.AkkaProtocolServer;
import city.sane.wot.binding.coap.CoapProtocolServer;
import city.sane.wot.binding.mqtt.MqttProtocolClientFactory;
import city.sane.wot.binding.mqtt.MqttProtocolServer;
import city.sane.wot.thing.action.ConsumedThingAction;
import city.sane.wot.thing.action.ThingAction;
import city.sane.wot.thing.event.ExposedThingEvent;
import city.sane.wot.thing.event.ThingEvent;
import city.sane.wot.thing.property.ConsumedThingProperty;
import city.sane.wot.thing.property.ExposedThingProperty;
import city.sane.wot.thing.property.ThingProperty;
import city.sane.wot.thing.schema.IntegerSchema;
import city.sane.wot.thing.schema.ObjectSchema;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import io.reactivex.rxjava3.disposables.Disposable;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Timeout;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.ArgumentsProvider;
import org.junit.jupiter.params.provider.ArgumentsSource;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletionException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Stream;

import static java.util.concurrent.TimeUnit.SECONDS;
import static org.awaitility.Awaitility.await;
import static org.awaitility.Awaitility.fieldIn;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

public class ConsumedThingIT {
    private Servient servient;

    @AfterEach
    public void teardown() {
        servient.shutdown().join();
    }

    @ParameterizedTest
    @ArgumentsSource(MyArgumentsProvider.class)
    @Timeout(value = 20, unit = SECONDS)
    public void readPropertyShouldReturnCorrectValue(Class server, Class clientFactory) throws ExecutionException, InterruptedException, ServientException {
        Config config = ConfigFactory
                .parseString("wot.servient.servers = [\"" + server.getName() + "\"]\n" +
                        "wot.servient.client-factories = [\"" + clientFactory.getName() + "\"]")
                .withFallback(ConfigFactory.load());
        servient = new Servient(config);
        servient.start().join();

        ExposedThing exposedThing = getExposedCounterThing();
        servient.addThing(exposedThing);
        exposedThing.expose().join();

        ConsumedThing thing = new ConsumedThing(servient, exposedThing);

        ConsumedThingProperty<Object> counter = thing.getProperty("count");

        try {
            assertEquals(42, counter.read().get());
        }
        catch (CompletionException e) {
            if (!(e.getCause() instanceof NoFormForInteractionConsumedThingException)) {
                throw e;
            }
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
    @Timeout(value = 20, unit = SECONDS)
    public void writePropertyShouldUpdateValue(Class server, Class clientFactory) throws ExecutionException, InterruptedException, ServientException {
        Config config = ConfigFactory
                .parseString("wot.servient.servers = [\"" + server.getName() + "\"]\n" +
                        "wot.servient.client-factories = [\"" + clientFactory.getName() + "\"]")
                .withFallback(ConfigFactory.load());
        servient = new Servient(config);
        servient.start().join();

        ExposedThing exposedThing = getExposedCounterThing();
        servient.addThing(exposedThing);
        exposedThing.expose().join();

        ConsumedThing thing = new ConsumedThing(servient, exposedThing);

        ConsumedThingProperty<Object> counter = thing.getProperty("count");

        try {
            counter.write(1337).get();
            assertEquals(1337, counter.read().get());
        }
        catch (CompletionException e) {
            if (!(e.getCause() instanceof NoFormForInteractionConsumedThingException)) {
                throw e;
            }
        }
    }

    @ParameterizedTest
    @ArgumentsSource(MyArgumentsProvider.class)
    public void observePropertyShouldHandleMultipleSubscription(Class server, Class clientFactory) throws ServientException, InterruptedException, ExecutionException {
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

            ExposedThingProperty<Object> property = exposedThing.getProperty("count");

            ConsumedThing consumedThing = new ConsumedThing(servient, exposedThing);

            List<Object> results1 = new ArrayList<>();
            Disposable disposable1 = consumedThing.getProperty("count").observer()
                    .subscribe(next -> results1.add(next.orElse(null)));

            List<Object> results2 = new ArrayList<>();
            Disposable disposable2 = consumedThing.getProperty("count").observer()
                    .subscribe(next -> results2.add(next.orElse(null)));

            // wait until boths clients have established subscriptions
            await()
                    .atMost(Duration.ofSeconds(10))
                    .until(() -> {
                        Object[] subscribers = (Object[]) fieldIn(property.getState().getSubject())
                                .ofType(AtomicReference.class)
                                .andWithName("subscribers").call().get();
                        if (server == CoapProtocolServer.class || server == MqttProtocolServer.class) {
                            // TODO: These bindings require only one Observer. Therefore, we cannot
                            //  check whether the two clients have actually subscribed. Therefore
                            //  race conditions can occur here
                            return subscribers.length == 1;
                        }
                        else {
                            return subscribers.length == 2;
                        }
                    });

            property.write(1337).get();

            // wait until boths clients have established subscriptions
            await()
                    .atMost(Duration.ofSeconds(10))
                    .until(() -> {
                        Object[] subscribers = (Object[]) fieldIn(property.getState().getSubject())
                                .ofType(AtomicReference.class)
                                .andWithName("subscribers").call().get();
                        if (server == CoapProtocolServer.class || server == MqttProtocolServer.class) {
                            // TODO: These bindings require only one Observer. Therefore, we cannot
                            //  check whether the two clients have actually subscribed. Therefore
                            //  race conditions can occur here
                            return subscribers.length == 1;
                        }
                        else {
                            return subscribers.length == 2;
                        }
                    });

            property.write(1338).get();

            // wait until boths clients have received all results
            await().atMost(Duration.ofSeconds(10)).untilAsserted(() -> {
                assertThat(results1, contains(1337, 1338));
                assertThat(results2, contains(1337, 1338));
            });

            disposable1.dispose();
            disposable2.dispose();
        }
        catch (ExecutionException e) {
            if (!(e.getCause() instanceof ProtocolClientNotImplementedException)) {
                throw e;
            }
        }
    }

    @ParameterizedTest
    @ArgumentsSource(MyArgumentsProvider.class)
    @Timeout(value = 20, unit = SECONDS)
    public void readPropertiesShouldReturnCorrectValues(Class server, Class clientFactory) throws ExecutionException, InterruptedException, ServientException {
        Config config = ConfigFactory
                .parseString("wot.servient.servers = [\"" + server.getName() + "\"]\n" +
                        "wot.servient.client-factories = [\"" + clientFactory.getName() + "\"]")
                .withFallback(ConfigFactory.load());
        servient = new Servient(config);
        servient.start().join();

        ExposedThing exposedThing = getExposedCounterThing();
        servient.addThing(exposedThing);
        exposedThing.expose().join();

        ConsumedThing thing = new ConsumedThing(servient, exposedThing);

        try {
            Map values = thing.readProperties().get();
            assertEquals(2, values.size());
            assertEquals(42, values.get("count"));
        }
        catch (ExecutionException e) {
            if (!(e.getCause() instanceof NoFormForInteractionConsumedThingException)) {
                throw e;
            }
        }
    }

    @ParameterizedTest
    @ArgumentsSource(MyArgumentsProvider.class)
    @Timeout(value = 20, unit = SECONDS)
    public void readMultipleProperties(Class server, Class clientFactory) throws ExecutionException, InterruptedException, ServientException {
        Config config = ConfigFactory
                .parseString("wot.servient.servers = [\"" + server.getName() + "\"]\n" +
                        "wot.servient.client-factories = [\"" + clientFactory.getName() + "\"]")
                .withFallback(ConfigFactory.load());
        servient = new Servient(config);
        servient.start().join();

        ExposedThing exposedThing = getExposedCounterThing();
        servient.addThing(exposedThing);
        exposedThing.expose().join();

        ConsumedThing thing = new ConsumedThing(servient, exposedThing);

        try {
            Map values = thing.readProperties("count").get();
            assertEquals(1, values.size());
            assertEquals(42, values.get("count"));
        }
        catch (ExecutionException e) {
            if (!(e.getCause() instanceof NoFormForInteractionConsumedThingException)) {
                throw e;
            }
        }
    }

    @ParameterizedTest
    @ArgumentsSource(MyArgumentsProvider.class)
    @Timeout(value = 20, unit = SECONDS)
    public void invokeActionShouldExecuteDefinedTask(Class server, Class clientFactory) throws ExecutionException, InterruptedException, ServientException {
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

            ConsumedThing thing = new ConsumedThing(servient, exposedThing);

            ConsumedThingAction increment = thing.getAction("increment");
            Object output = increment.invoke().get();

            if (MqttProtocolClientFactory.class.isAssignableFrom(clientFactory)) {
                // mqtt is not able to return a result
                assertNull(output);
            }
            else {
                assertEquals(43, output);
            }
        }
        catch (ExecutionException e) {
            if (!(e.getCause() instanceof ProtocolClientNotImplementedException)) {
                throw e;
            }
        }
    }

    @ParameterizedTest
    @ArgumentsSource(MyArgumentsProvider.class)
    @Timeout(value = 20, unit = SECONDS)
    public void invokeActionWithParametersShouldExecuteDefinedTask(Class server, Class clientFactory) throws ExecutionException, InterruptedException, ServientException {
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

            ConsumedThing thing = new ConsumedThing(servient, exposedThing);

            ConsumedThingAction increment = thing.getAction("increment");
            Object output = increment.invoke(Map.of("step", 3)).get();

            if (MqttProtocolClientFactory.class.isAssignableFrom(clientFactory)) {
                // mqtt is not able to return a result
                assertNull(output);
            }
            else {
                assertEquals(45, output);
            }
        }
        catch (ExecutionException e) {
            if (!(e.getCause() instanceof ProtocolClientNotImplementedException)) {
                throw e;
            }
        }
    }

    @ParameterizedTest
    @ArgumentsSource(MyArgumentsProvider.class)
    public void observeEventShouldHandleMultipleSubscription(Class server, Class clientFactory) throws ServientException {
        Config config = ConfigFactory
                .parseString("wot.servient.servers = [\"" + server.getName() + "\"]\n" +
                        "wot.servient.client-factories = [\"" + clientFactory.getName() + "\"]")
                .withFallback(ConfigFactory.load());
        servient = new Servient(config);
        servient.start().join();

        ExposedThing exposedThing = getExposedCounterThing();
        servient.addThing(exposedThing);
        exposedThing.expose().join();

        ExposedThingEvent<Object> event = exposedThing.getEvent("change");

        ConsumedThing consumedThing = new ConsumedThing(servient, exposedThing);

        List<Object> results1 = new ArrayList<>();
        Disposable disposable1 = consumedThing.getEvent("change").observer()
                .subscribe(next -> results1.add(next.orElse(null)));

        List<Object> results2 = new ArrayList<>();
        Disposable disposable2 = consumedThing.getEvent("change").observer()
                .subscribe(next -> results2.add(next.orElse(null)));

        // wait until boths clients have established subscriptions
        await()
                .atMost(Duration.ofSeconds(10))
                .until(() -> {
                    Object[] subscribers = (Object[]) fieldIn(event.getState().getSubject())
                            .ofType(AtomicReference.class)
                            .andWithName("subscribers").call().get();
                    if (server == CoapProtocolServer.class || server == MqttProtocolServer.class) {
                        // TODO: These bindings require only one Observer. Therefore, we cannot
                        //  check whether the two clients have actually subscribed. Therefore
                        //  race conditions can occur here
                        return subscribers.length == 1;
                    }
                    else {
                        return subscribers.length == 2;
                    }
                });

        event.emit();

        // wait until boths clients have established subscriptions
        await()
                .atMost(Duration.ofSeconds(10))
                .until(() -> {
                    Object[] subscribers = (Object[]) fieldIn(event.getState().getSubject())
                            .ofType(AtomicReference.class)
                            .andWithName("subscribers").call().get();
                    if (server == CoapProtocolServer.class || server == MqttProtocolServer.class) {
                        // TODO: These bindings require only one Observer. Therefore, we cannot
                        //  check whether the two clients have actually subscribed. Therefore
                        //  race conditions can occur here
                        return subscribers.length == 1;
                    }
                    else {
                        return subscribers.length == 2;
                    }
                });

        event.emit();

        // wait until boths clients have received all results
        await().atMost(Duration.ofSeconds(10)).untilAsserted(() -> {
            assertThat(results1, contains(nullValue(), nullValue()));
            assertThat(results2, contains(nullValue(), nullValue()));
        });

        disposable1.dispose();
        disposable2.dispose();
    }

    private static class MyArgumentsProvider implements ArgumentsProvider {
        public MyArgumentsProvider() {
        }

        @Override
        public Stream<? extends Arguments> provideArguments(
                ExtensionContext extensionContext) {
            return Stream.of(
                    Arguments.of(AkkaProtocolServer.class, AkkaProtocolClientFactory.class)
            );
        }
    }
}
