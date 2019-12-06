package city.sane.wot.binding.http;

import city.sane.wot.thing.security.SecurityScheme;
import com.typesafe.config.ConfigFactory;
import org.junit.Test;

import java.util.Collections;
import java.util.List;

import static org.junit.Assert.assertTrue;

public class HttpProtocolClientTest {
    @Test
    public void setSecurity() {
        HttpProtocolClient client = new HttpProtocolClient();

        SecurityScheme securityScheme = new SecurityScheme();
        securityScheme.setScheme("basic");
        List<SecurityScheme> metadata = Collections.singletonList(securityScheme);

        Object credentials = ConfigFactory
                .parseString("credentials { username = \"foo\"\npassword = \"bar\" }").getObject("credentials");

        assertTrue(client.setSecurity(metadata, credentials));
    }
}