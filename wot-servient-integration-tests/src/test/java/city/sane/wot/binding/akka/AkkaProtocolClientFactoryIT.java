package city.sane.wot.binding.akka;

import city.sane.relay.server.RelayServer;
import com.typesafe.config.ConfigFactory;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.concurrent.ExecutionException;

import static org.junit.Assert.assertNull;

public class AkkaProtocolClientFactoryIT {

    private Thread serverThread;
    private RelayServer server;

    @Before
    public void setUp() {
        server = new RelayServer(ConfigFactory.load());
        serverThread = new Thread(server);
        serverThread.start();
    }

    @After
    public void tearDown() throws InterruptedException {
        server.close();
        serverThread.join();
    }

    @Test
    public void init() throws ExecutionException, InterruptedException {
        AkkaProtocolClientFactory factory = new AkkaProtocolClientFactory(ConfigFactory.load());

        assertNull(factory.init().get());
    }

    @Test
    public void destroy() throws ExecutionException, InterruptedException {
        AkkaProtocolClientFactory factory = new AkkaProtocolClientFactory(ConfigFactory.load());
        factory.init().join();

        assertNull(factory.destroy().get());
    }
}