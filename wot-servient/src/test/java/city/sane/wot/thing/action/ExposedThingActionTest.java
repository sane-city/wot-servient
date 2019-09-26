package city.sane.wot.thing.action;

import city.sane.wot.thing.ExposedThing;
import city.sane.wot.thing.Thing;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class ExposedThingActionTest {
    private ExposedThing thing;
    private ExposedThingAction action;

    @Before
    public void setUp() {
        thing = new ExposedThing(null, new Thing.Builder().setId("ThingA").build());
        thing.addAction("foo", new ThingAction(), (input, options) -> {
            System.out.println(input);
            System.out.println(options);
            return null;
        });
        action = thing.getAction("foo");
    }

    @After
    public void tearDown() {
    }

    @Test
    public void invokeWithHandler() {
    }
}