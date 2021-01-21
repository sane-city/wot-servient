package city.sane.wot.binding.akka;

import com.typesafe.config.ConfigFactory;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.util.concurrent.ExecutionException;

import static org.junit.Assert.assertNull;

public class AkkaProtocolClientFactoryIT {
    private AkkaProtocolClientFactory factory = null;

    @AfterEach
    public void tearDown() {
        if (factory != null) {
            factory.destroy().join();
        }
    }

    @Test
    public void initShouldNotFail() throws ExecutionException, InterruptedException {
        factory = new AkkaProtocolClientFactory(ConfigFactory.load());

        assertNull(factory.init().get());
    }

    @Test
    public void destroyShouldNotFail() throws ExecutionException, InterruptedException {
        factory = new AkkaProtocolClientFactory(ConfigFactory.load());
        factory.init().join();

        assertNull(factory.destroy().get());
    }
}