package city.sane.wot.thing;

import city.sane.wot.thing.action.ExposedThingAction;
import city.sane.wot.thing.action.ThingAction;
import city.sane.wot.thing.event.ExposedThingEvent;
import city.sane.wot.thing.event.ThingEvent;
import city.sane.wot.thing.property.ExposedThingProperty;
import city.sane.wot.thing.property.ThingProperty;
import com.github.jsonldjava.shaded.com.google.common.base.Supplier;
import org.junit.Test;

import java.util.Date;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;

import static org.junit.Assert.assertEquals;

public class ExposedThingTest {
    @Test
    public void readProperty() throws ExecutionException, InterruptedException {
        ExposedThing thing = getCounterThing();

        assertEquals(42, thing.getProperty("count").read().get());
    }

    @Test
    public void writeProperty() throws ExecutionException, InterruptedException {
        ExposedThing thing = getCounterThing();
        thing.getProperty("count").write(1337).join();

        ExposedThingProperty counter = thing.getProperty("count");
        assertEquals(1337, counter.read().get());
    }

    @Test
    public void invokeAction() throws ExecutionException, InterruptedException {
        ExposedThing thing = getCounterThing();
        ExposedThingAction increment = thing.getAction("increment");

        assertEquals(43, increment.invoke().get());
    }

    @Test
    public void emitEvent() {
        ExposedThing thing = getCounterThing();
        thing.getEvent("change").emit();
    }

    @Test
    public void subscribeEvent() {
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
    public void observeProperty() {
        ExposedThing thing = getCounterThing();
        ExposedThingProperty property = thing.getProperty("count");

        CompletableFuture<Object> subscriptionFuture = new CompletableFuture<>();
        property.subscribe(
                subscriptionFuture::complete,
                subscriptionFuture::completeExceptionally,
                () -> System.out.println("completed!"));
        property.write(1337);

        subscriptionFuture.join();
    }

    @Test
    public void allProperties() throws ExecutionException, InterruptedException {
        ExposedThing thing = getCounterThing();
        Map<String, Object> values = thing.readProperties().get();

        assertEquals(42, values.get("count"));
    }

    @Test
    public void jsonSerialization() {
        ExposedThing thing = getCounterThing();

        String json = thing.toJson();

        System.out.println(json);
//        assertEquals("{\"id\":\"counter\",\"title\":\"counter\",\"description\":\"counter example Thing\",\"properties\":{\"lastChange\":{\"description\":\"last change of counter value\",\"type\":\"string\",\"observable\":true,\"readOnly\":true},\"counter\":{\"description\":\"current counter value\",\"type\":\"integer\",\"observable\":true,\"readOnly\":true}},\"actions\":{\"decrement\":{},\"increment\":{},\"reset\":{}},\"events\":{\"change\":{}}}", json);
    }

    @Test
    public void propertyWithHandlers() throws ExecutionException, InterruptedException {
        ExposedThing thing = getCounterThing();

        Supplier<CompletableFuture<Object>> readHandler = () -> CompletableFuture.completedFuture(1337);
        Function<Object, CompletableFuture<Object>> writeHandler = (value) -> CompletableFuture.completedFuture(((int) value) / 2);

        thing.addProperty("withHandlers", new ThingProperty(), readHandler, writeHandler);
        ExposedThingProperty property = thing.getProperty("withHandlers");

        assertEquals(1337, property.read().get());
        assertEquals(2, property.write(4).get());
    }

    private ExposedThing getCounterThing() {
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

        ExposedThing thing = new ExposedThing(null)
                .setId("counter")
                .setTitle("counter")
                .setDescription("counter example Thing");

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

        return thing;
    }
}
