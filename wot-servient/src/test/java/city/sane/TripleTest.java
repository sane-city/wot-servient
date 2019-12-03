package city.sane;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

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
}
