package city.sane.wot.binding.jadex;

import com.typesafe.config.ConfigFactory;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertEquals;

public class JadexProtocolClientFactoryTest {
    @Test
    public void getScheme() {
        assertEquals("jadex", new JadexProtocolClientFactory(ConfigFactory.load()).getScheme());
    }

    @Test
    public void getClient() {
        assertThat(new JadexProtocolClientFactory(ConfigFactory.load()).getClient(), instanceOf(JadexProtocolClient.class));
    }
}