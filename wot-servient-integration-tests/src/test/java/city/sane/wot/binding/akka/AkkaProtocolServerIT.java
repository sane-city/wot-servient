package city.sane.wot.binding.akka;

import com.typesafe.config.ConfigFactory;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.net.URI;
import java.net.URISyntaxException;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class AkkaProtocolServerIT {
    private AkkaProtocolServer server;

    @BeforeEach
    public void setUp() {
        server = new AkkaProtocolServer(ConfigFactory.load());
        server.start(null).join();
    }

    @AfterEach
    public void tearDown() {
        server.stop().join();
    }

    @Test
    public void getDirectoryUrlShouldReturnCorredUrl() throws URISyntaxException {
        assertEquals(new URI("akka://wot@127.0.0.1:25520/user/things#thing-directory"), server.getDirectoryUrl());
    }
}