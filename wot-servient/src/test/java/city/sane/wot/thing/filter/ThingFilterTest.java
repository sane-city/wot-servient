package city.sane.wot.thing.filter;

import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class ThingFilterTest {
    @Test
    public void testEquals() {
        ThingFilter filterA = new ThingFilter().setMethod(DiscoveryMethod.ANY);
        ThingFilter filterB = new ThingFilter().setMethod(DiscoveryMethod.ANY);
        ThingFilter filterC = new ThingFilter().setMethod(DiscoveryMethod.LOCAL);

        assertTrue(filterA.equals(filterB));
        assertTrue(filterB.equals(filterA));
        assertFalse(filterA.equals(filterC));
        assertFalse(filterC.equals(filterA));
    }
}