package city.sane.wot.thing.security;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class SecuritySchemeTest {
    @Test
    public void setScheme() {
        SecurityScheme scheme = new SecurityScheme();
        scheme.setScheme("basic_sc");

        assertEquals("basic_sc", scheme.getScheme());
    }
}