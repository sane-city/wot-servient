package city.sane.wot.binding.akka;

import com.typesafe.config.ConfigFactory;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.net.URI;
import java.net.URISyntaxException;

import static org.junit.Assert.assertEquals;

public class AkkaProtocolServerIT {
    private AkkaProtocolServer server;

    @Before
    public void setUp() {
        server = new AkkaProtocolServer(ConfigFactory.load());
        server.start(null).join();
    }

    @After
    public void tearDown() {
        server.stop().join();
    }

    @Test
    public void getDirectoryUrlShouldReturnCorredUrl() throws URISyntaxException {
        assertEquals(new URI("akka://wot@127.0.0.1:25520/user/things#thing-directory"), server.getDirectoryUrl());
    }
}