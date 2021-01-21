package city.sane.wot.binding.jsonpathhttp;

import org.junit.jupiter.api.Test;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class JsonpathHttpProtocolClientFactoryTest {
    @Test
    public void getScheme() {
        assertEquals("jsonpath+http", new JsonpathHttpProtocolClientFactory().getScheme());
    }

    @Test
    public void getClient() {
        assertThat(new JsonpathHttpProtocolClientFactory().getClient(), instanceOf(JsonpathHttpProtocolClient.class));
    }
}