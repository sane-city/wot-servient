package city.sane.wot.binding.jadex;

import com.typesafe.config.ConfigFactory;
import org.junit.jupiter.api.Test;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class JadexProtocolClientFactoryTest {
    @Test
    public void getSchemeShouldReturnCorrectScheme() {
        assertEquals("jadex", new JadexProtocolClientFactory(ConfigFactory.load()).getScheme());
    }

    @Test
    public void getClientShouldReturnJadexClient() {
        assertThat(new JadexProtocolClientFactory(ConfigFactory.load()).getClient(), instanceOf(JadexProtocolClient.class));
    }
}