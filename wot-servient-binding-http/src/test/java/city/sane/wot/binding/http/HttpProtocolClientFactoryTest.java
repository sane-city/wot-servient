package city.sane.wot.binding.http;

import org.junit.Test;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertEquals;

public class HttpProtocolClientFactoryTest {
    @Test
    public void getScheme() {
        assertEquals("http", new HttpProtocolClientFactory(null).getScheme());
    }

    @Test
    public void getClient() {
        assertThat(new HttpProtocolClientFactory(null).getClient(), instanceOf(HttpProtocolClient.class));
    }
}