package city.sane.wot.thing.property;

import city.sane.wot.thing.ExposedThing;
import city.sane.wot.thing.Thing;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class ExposedThingPropertyTest {
    private ExposedThing thing;
    private ExposedThingProperty property;

    @Before
    public void setUp() {
        thing = new ExposedThing(null, new Thing.Builder().setId("ThingA").build());
        thing.addProperty("foo", new ThingProperty());
        property = thing.getProperty("foo");
    }

    @After
    public void tearDown() {
    }

    @Test
    public void read() throws ExecutionException, InterruptedException {
        property.write(1337).join();

        assertEquals(1337, property.read().get());
    }

    @Test
    public void readWithHandler() throws InterruptedException, ExecutionException {
        property.write(2).join();
        property.getState().setReadHandler(() -> {
            int value = (int) thing.getProperty("foo").getState().getValue();
            return CompletableFuture.completedFuture((int) Math.pow(value, 2));
        });

        assertEquals(4, property.read().get());
        assertEquals(16, property.read().get());
    }

    @Test
    public void write() throws ExecutionException, InterruptedException {
        assertNull(property.write(1337).get());
    }

    @Test
    public void writeWithHandler() throws InterruptedException, ExecutionException {
        property.getState().setWriteHandler((value) -> CompletableFuture.completedFuture(((int) value) / 2));

        assertEquals(50, property.write(100).get());
        assertEquals(25, property.write(50).get());
    }
}