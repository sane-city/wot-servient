package city.sane.wot.binding.http;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class HttpsProtocolClientFactoryTest {
    @Test
    public void getScheme() {
        assertEquals("https", new HttpsProtocolClientFactory().getScheme());
    }
}