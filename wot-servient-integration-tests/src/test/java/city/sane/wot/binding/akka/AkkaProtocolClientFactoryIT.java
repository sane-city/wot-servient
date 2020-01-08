package city.sane.wot.binding.akka;

import akka.actor.ActorSystem;
import akka.testkit.javadsl.TestKit;
import city.sane.relay.server.RelayServer;
import com.typesafe.config.ConfigFactory;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.concurrent.ExecutionException;

import static org.junit.Assert.assertNull;

public class AkkaProtocolClientFactoryIT {
    private RelayServer relayServer;
    private Thread relayServerThread;

    @Before
    public void setUp() {
        relayServer = new RelayServer(ConfigFactory.load());
        relayServerThread = new Thread(relayServer);
        relayServerThread.start();
    }

    @After
    public void tearDown() throws InterruptedException {
        relayServer.close();
        relayServerThread.join();
    }

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