package city.sane.wot.thing.action;

import city.sane.wot.thing.ExposedThing;
import org.junit.Test;

import static org.junit.Assert.assertNotNull;

public class ExposedThingActionTest {
    @Test
    public void invokeEnsureNonNullReturnValue() {
        ExposedThing thing = new ExposedThing(null);
        ThingAction action = new ThingAction.Builder().build();
        ExposedThingAction exposedAction = new ExposedThingAction(null, action, thing);
        exposedAction.getState().setHandler((o, stringObjectMap) -> null);

        assertNotNull(exposedAction.invoke());
    }
}