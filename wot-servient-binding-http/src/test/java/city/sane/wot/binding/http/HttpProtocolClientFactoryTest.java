package city.sane.wot.binding.http;

import org.junit.Test;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertEquals;

public class HttpProtocolClientFactoryTest {
    @Test
    public void getScheme() {
        assertEquals("http", new HttpProtocolClientFactory().getScheme());
    }

    @Test
    public void getClient() {
        assertThat(new HttpProtocolClientFactory().getClient(), instanceOf(HttpProtocolClient.class));
    }
}