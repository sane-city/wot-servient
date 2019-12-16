package city.sane.wot.binding.akka;

import com.typesafe.config.ConfigFactory;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertEquals;

public class AkkaProtocolClientFactoryTest {
    @Test
    public void getScheme() {
        assertEquals("bud", new AkkaProtocolClientFactory(ConfigFactory.load()).getScheme());
    }

    @Test
    public void getClient() {
        assertThat(new AkkaProtocolClientFactory(ConfigFactory.load()).getClient(), instanceOf(AkkaProtocolClient.class));
    }
}