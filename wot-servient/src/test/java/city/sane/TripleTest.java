package city.sane;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

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

        assertEquals(tripleA, tripleB);
        assertEquals(tripleB, tripleA);
        assertNotEquals(tripleA, tripleC);
        assertNotEquals(tripleC, tripleA);
    }
}
