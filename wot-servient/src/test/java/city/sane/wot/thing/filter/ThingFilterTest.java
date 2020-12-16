package city.sane.wot.thing.filter;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

public class ThingFilterTest {
    @Test
    public void testEquals() {
        ThingFilter filterA = new ThingFilter().setMethod(DiscoveryMethod.ANY);
        ThingFilter filterB = new ThingFilter().setMethod(DiscoveryMethod.ANY);
        ThingFilter filterC = new ThingFilter().setMethod(DiscoveryMethod.LOCAL);

        assertEquals(filterA, filterB);
        assertEquals(filterB, filterA);
        assertNotEquals(filterA, filterC);
        assertNotEquals(filterC, filterA);
    }
}