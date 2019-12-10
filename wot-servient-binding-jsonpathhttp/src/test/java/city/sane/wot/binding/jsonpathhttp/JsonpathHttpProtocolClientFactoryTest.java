package city.sane.wot.binding.jsonpathhttp;

import org.junit.Test;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertEquals;

public class JsonpathHttpProtocolClientFactoryTest {
    @Test
    public void getScheme() {
        assertEquals("jsonpath+http", new JsonpathHttpProtocolClientFactory(null).getScheme());
    }

    @Test
    public void getClient() {
        assertThat(new JsonpathHttpProtocolClientFactory(null).getClient(), instanceOf(JsonpathHttpProtocolClient.class));
    }
}