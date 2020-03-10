package city.sane.wot.binding.http;

import city.sane.wot.binding.ProtocolServerException;
import city.sane.wot.thing.security.BasicSecurityScheme;
import city.sane.wot.thing.security.SecurityScheme;
import com.typesafe.config.ConfigFactory;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.hamcrest.text.MatchesPattern.matchesPattern;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

public class HttpProtocolServerIT {
    private HttpProtocolServer server;

    @Before
    public void setUp() throws ProtocolServerException {
        server = new HttpProtocolServer(ConfigFactory.load());
        server.start(null).join();
    }

    @After
    public void tearDown() {
        server.stop().join();
    }

    @Test
    public void getDirectoryUrl() {
        String url = server.getDirectoryUrl().toString();

        assertThat(url, matchesPattern("http://.*:\\d+"));
    }

    @Test
    public void getThingUrl() {
        String url = server.getThingUrl("counter").toString();

        assertThat(url, matchesPattern("http://.*:\\d+/counter"));
    }

    @Test
    public void setSecurity() {
        HttpProtocolClient client = new HttpProtocolClient();

        SecurityScheme securityScheme = new BasicSecurityScheme();
        List<SecurityScheme> metadata = Collections.singletonList(securityScheme);

        assertTrue(client.setSecurity(metadata, Map.of("username", "foo", "password", "bar")));
    }
}