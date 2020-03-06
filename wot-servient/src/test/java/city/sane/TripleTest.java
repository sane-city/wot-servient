package city.sane;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class TripleTest {
    @Test
    public void first() {
        Triple pair = new Triple<>(10, false, "beers");

        assertEquals(10, pair.first());
    }

    @Test
    public void second() {
        Triple pair = new Triple<>(10, false, "beers");

        assertEquals(false, pair.second());
    }

    @Test
    public void third() {
        Triple pair = new Triple<>(10, false, "beers");

        assertEquals("beers", pair.third());
    }

    @Test
    public void testEquals() {
        Triple tripleA = new Triple(1, "cold", "beer");
        Triple tripleB = new Triple(1, "cold", "beer");
        Triple tripleC = new Triple(1, "warm", "beer");

        assertTrue(tripleA.equals(tripleB));
        assertTrue(tripleB.equals(tripleA));
        assertFalse(tripleA.equals(tripleC));
        assertFalse(tripleC.equals(tripleA));
    }
}
