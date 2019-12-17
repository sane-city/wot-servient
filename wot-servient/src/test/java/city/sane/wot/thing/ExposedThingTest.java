package city.sane.wot.thing;

import city.sane.wot.Servient;
import city.sane.wot.ServientException;
import city.sane.wot.binding.ProtocolServer;
import city.sane.wot.thing.action.ExposedThingAction;
import city.sane.wot.thing.action.ThingAction;
import city.sane.wot.thing.event.ExposedThingEvent;
import city.sane.wot.thing.event.ThingEvent;
import city.sane.wot.thing.property.ExposedThingProperty;
import city.sane.wot.thing.property.ThingProperty;
import com.github.jsonldjava.shaded.com.google.common.base.Supplier;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import org.junit.Test;

import java.util.Date;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiConsumer;
import java.util.function.Function;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.*;

public class ExposedThingTest {
    @Test
    public void readProperty() throws ExecutionException, InterruptedException, ServientException {
        ExposedThing thing = getCounterThing();

        assertEquals(42, thing.getProperty("count").read().get());
    }

    @Test
    public void writeProperty() throws ExecutionException, InterruptedException, ServientException {
        ExposedThing thing = getCounterThing();
        thing.getProperty("count").write(1337).join();

        ExposedThingProperty counter = thing.getProperty("count");
        assertEquals(1337, counter.read().get());
    }

    @Test
    public void invokeAction() throws ExecutionException, InterruptedException, ServientException {
        ExposedThing thing = getCounterThing();
        ExposedThingAction increment = thing.getAction("increment");

        assertEquals(43, increment.invoke().get());
    }

    @Test
    public void emitEvent() throws ExecutionException, InterruptedException, ServientException {
        ExposedThing thing = getCounterThing();

        assertNull(thing.getEvent("change").emit().get());
    }

    @Test
    public void subscribeEvent() throws ServientException {
        ExposedThing thing = getCounterThing();
        ExposedThingEvent event = thing.getEvent("change");

        AtomicInteger counter1 = new AtomicInteger();
        event.subscribe(next -> counter1.getAndIncrement());

        AtomicInteger counter2 = new AtomicInteger();
        event.subscribe(next -> counter2.getAndIncrement());

        event.emit().join();
        event.emit().join();

        assertEquals(2, counter1.get());
        assertEquals(2, counter2.get());
    }

    @Test
    public void observeProperty() throws ExecutionException, InterruptedException, ServientException {
        ExposedThing thing = getCounterThing();
        ExposedThingProperty property = thing.getProperty("count");

        CompletableFuture<Object> subscriptionFuture = new CompletableFuture<>();
        property.subscribe(
                subscriptionFuture::complete,
                subscriptionFuture::completeExceptionally,
                () -> System.out.println("completed!"));
        property.write(1337);

        assertEquals(1337, subscriptionFuture.get());
    }

    @Test
    public void allProperties() throws ExecutionException, InterruptedException, ServientException {
        ExposedThing thing = getCounterThing();
        Map<String, Object> values = thing.readProperties().get();

        assertEquals(42, values.get("count"));
    }

    @Test
    public void jsonSerialization() throws ServientException {
        ExposedThing thing = getCounterThing();

        String json = thing.toJson();

        System.out.println(json);

//        assertEquals("{\"id\":\"counter\",\"title\":\"counter\",\"description\":\"counter example Thing\",\"properties\":{\"lastChange\":{\"description\":\"last change of counter value\",\"type\":\"string\",\"observable\":true,\"readOnly\":true},\"counter\":{\"description\":\"current counter value\",\"type\":\"integer\",\"observable\":true,\"readOnly\":true}},\"actions\":{\"decrement\":{},\"increment\":{},\"reset\":{}},\"events\":{\"change\":{}}}", json);
        assertThat(json, instanceOf(String.class));
    }

    @Test
    public void propertyWithHandlers() throws ExecutionException, InterruptedException, ServientException {
        ExposedThing thing = getCounterThing();

        Supplier<CompletableFuture<Object>> readHandler = () -> CompletableFuture.completedFuture(1337);
        Function<Object, CompletableFuture<Object>> writeHandler = (value) -> CompletableFuture.completedFuture(((int) value) / 2);

        thing.addProperty("withHandlers", new ThingProperty(), readHandler, writeHandler);
        ExposedThingProperty property = thing.getProperty("withHandlers");

        assertEquals(1337, property.read().get());
        assertEquals(2, property.write(4).get());
    }

    @Test
    public void expose() throws ExecutionException, InterruptedException, ServientException {
        ExposedThing thing = getCounterThing();

        assertThat(thing.expose().get(), instanceOf(ExposedThing.class));
    }

    @Test
    public void destroy() throws ExecutionException, InterruptedException, ServientException {
        ExposedThing thing = getCounterThing();

        assertThat(thing.destroy().get(), instanceOf(ExposedThing.class));
    }

    @Test
    public void writeProperties() throws ServientException, ExecutionException, InterruptedException {
        ExposedThing thing = getCounterThing();

        Map<String, Object> newValues = Map.of("count", 0);
        assertThat(thing.writeProperties(newValues).get(), instanceOf(Map.class));
    }

    @Test
    public void addProperty() throws ServientException {
        ExposedThing thing = getCounterThing();

        thing.addProperty("myNewProperty");

        assertNotNull(thing.getProperty("myNewProperty"));
    }

    @Test
    public void addPropertyWithInitValue() throws ServientException, ExecutionException, InterruptedException {
        ExposedThing thing = getCounterThing();

        thing.addProperty("myNewProperty", new ThingProperty(),1337);

        assertNotNull(thing.getProperty("myNewProperty"));
        assertEquals(1337, thing.getProperty("myNewProperty").read().get());
    }

    @Test
    public void removeProperty() throws ServientException {
        ExposedThing thing = getCounterThing();

        thing.removeProperty("count");

        assertNull(thing.getProperty("count"));
    }

    @Test
    public void addAction() throws ServientException {
        ExposedThing thing = getCounterThing();

        thing.addAction("myNewAction");

        assertNotNull(thing.getAction("myNewAction"));
    }

    @Test
    public void addActionWithBiConsumerHandler() throws ServientException, ExecutionException, InterruptedException {
        ExposedThing thing = getCounterThing();

        CompletableFuture<Boolean> future = new CompletableFuture();
        thing.addAction("myNewAction", (BiConsumer<Object, Map<String, Object>>) (input, options) -> future.complete(true));

        assertNull(thing.getAction("myNewAction").invoke().get());
        assertTrue(future.get());
    }

    @Test
    public void addActionWithRunnableHandler() throws ServientException, ExecutionException, InterruptedException {
        ExposedThing thing = getCounterThing();

        CompletableFuture<Boolean> future = new CompletableFuture();
        thing.addAction("myNewAction", () -> future.complete(true));

        assertNull(thing.getAction("myNewAction").invoke().get());
        assertTrue(future.get());
    }

    @Test
    public void addActionWithSupplierHandler() throws ServientException, ExecutionException, InterruptedException {
        ExposedThing thing = getCounterThing();

        CompletableFuture<Object> future = new CompletableFuture();
        thing.addAction("myNewAction", () -> {
            future.complete(true);
            return future;
        });

        assertTrue((Boolean) thing.getAction("myNewAction").invoke().get());
        assertTrue((Boolean) future.get());
    }

    @Test
    public void removeAction() throws ServientException {
        ExposedThing thing = getCounterThing();

        thing.removeAction("increment");

        assertNull(thing.getAction("increment"));
    }

    @Test
    public void addEvent() throws ServientException {
        ExposedThing thing = getCounterThing();

        thing.addEvent("myNewEvent");

        assertNotNull(thing.getEvent("myNewEvent"));
    }

    @Test
    public void removeEvent() throws ServientException {
        ExposedThing thing = getCounterThing();

        thing.removeEvent("change");

        assertNull(thing.getEvent("change"));
    }

    private ExposedThing getCounterThing() throws ServientException {
        Config config = ConfigFactory
                .parseString("wot.servient.servers = [\"" + ExposedThingTest.MyProtocolServer.class.getName() + "\"]")
                .withFallback(ConfigFactory.load());
        Servient servient = new Servient(config);

        ExposedThing thing = new ExposedThing(servient)
                .setId("counter")
                .setTitle("counter")
                .setDescription("counter example Thing");

        ThingProperty counterProperty = new ThingProperty.Builder()
                .setType("integer")
                .setDescription("current counter value")
                .setObservable(true)
                .setReadOnly(true)
                .build();

        ThingProperty lastChangeProperty = new ThingProperty.Builder()
                .setType("string")
                .setDescription("last change of counter value")
                .setObservable(true)
                .setReadOnly(true)
                .build();

        thing.addProperty("count", counterProperty, 42);
        thing.addProperty("lastChange", lastChangeProperty, new Date().toString());

        thing.addAction("increment", new ThingAction(), (input, options) -> {
            return thing.getProperty("count").read().thenApply(value -> {
                int newValue = ((Integer) value) + 1;
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

        servient.addThing(thing);

        return thing;
    }

    @Test
    public void testEquals() {
        ExposedThing thingA = new ExposedThing(null, new Thing.Builder().setId("counter").build());
        ExposedThing thingB = new ExposedThing(null, new Thing.Builder().setId("counter").build());

        assertEquals(thingA, thingB);
    }

    public static class MyProtocolServer implements ProtocolServer {
        public MyProtocolServer() {

        }

        @Override
        public CompletableFuture<Void> start() {
            return CompletableFuture.completedFuture(null);
        }

        @Override
        public CompletableFuture<Void> stop() {
            return CompletableFuture.completedFuture(null);
        }

        @Override
        public CompletableFuture<Void> expose(ExposedThing thing) {
            return CompletableFuture.completedFuture(null);
        }

        @Override
        public CompletableFuture<Void> destroy(ExposedThing thing) {
            return CompletableFuture.completedFuture(null);
        }
    }
}
