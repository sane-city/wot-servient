package city.sane.wot.binding.jadex;

import org.junit.Before;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;

public class JadexProtocolClientFactoryTest {
    private JadexProtocolClientConfig clientConfig;

    @Before
    public void setUp() {
        clientConfig = mock(JadexProtocolClientConfig.class);
    }

    @Test
    public void getSchemeShouldReturnCorrectScheme() {
        assertEquals("jadex", new JadexProtocolClientFactory(clientConfig).getScheme());
    }

    @Test
    public void getClientShouldReturnJadexClient() {
        assertThat(new JadexProtocolClientFactory(clientConfig).getClient(), instanceOf(JadexProtocolClient.class));
    }
}