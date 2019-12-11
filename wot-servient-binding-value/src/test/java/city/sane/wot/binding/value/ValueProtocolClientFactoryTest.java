package city.sane.wot.binding.value;

import org.junit.Test;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertEquals;

public class ValueProtocolClientFactoryTest {
    @Test
    public void getScheme() {
        assertEquals("value", new ValueProtocolClientFactory().getScheme());
    }

    @Test
    public void getClient() {
        assertThat(new ValueProtocolClientFactory().getClient(), instanceOf(ValueProtocolClient.class));
    }
}