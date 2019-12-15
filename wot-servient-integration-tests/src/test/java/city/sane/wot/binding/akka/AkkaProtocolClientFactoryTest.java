package city.sane.wot.binding.akka;

import com.typesafe.config.ConfigFactory;
import org.junit.Test;

import java.util.concurrent.ExecutionException;

import static org.junit.Assert.assertNull;

public class AkkaProtocolClientFactoryTest {
    @Test
    public void init() throws ExecutionException, InterruptedException {
        assertNull(new AkkaProtocolClientFactory(ConfigFactory.load()).init().get());
    }

    @Test
    public void destroy() throws ExecutionException, InterruptedException {
        AkkaProtocolClientFactory factory = new AkkaProtocolClientFactory(ConfigFactory.load());
        factory.init().join();

        assertNull(factory.destroy().get());
    }
}