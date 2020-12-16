package city.sane.wot.thing.security;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class NoSecuritySchemeTest {
    @Nested
    class Equals {
        @Test
        void shouldAlwaysReturnTrue() {
            assertEquals(new NoSecurityScheme(), new NoSecurityScheme());
        }
    }

    @Nested
    class HashCode {
        @Test
        void shouldAlwaysReturnSameHashCode() {
            assertEquals(new NoSecurityScheme().hashCode(), new NoSecurityScheme().hashCode());
        }
    }
}