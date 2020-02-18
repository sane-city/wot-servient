package city.sane.wot.binding.http;

import city.sane.wot.binding.ProtocolServerException;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigObject;
import org.junit.Before;
import org.junit.Test;

import java.util.Map;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class HttpProtocolServerTest {
    private Config config;
    private ConfigObject configObject;

    @Before
    public void setUp() {
        config = mock(Config.class);
        configObject = mock(ConfigObject.class);
    }

    @Test(expected = ProtocolServerException.class)
    public void constructorShouldRejectInvalidSecurityScheme() throws ProtocolServerException {
        when(configObject.unwrapped()).thenReturn(Map.of("scheme", "dsadadas"));
        when(config.getObject("wot.servient.http.security")).thenReturn(configObject);

        new HttpProtocolServer(config);
    }
}