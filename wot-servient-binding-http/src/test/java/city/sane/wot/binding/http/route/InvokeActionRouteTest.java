package city.sane.wot.binding.http.route;

import city.sane.wot.Servient;
import city.sane.wot.ServientException;
import city.sane.wot.content.Content;
import city.sane.wot.content.ContentCodecException;
import city.sane.wot.content.ContentManager;
import city.sane.wot.thing.ExposedThing;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.entity.ContentType;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.util.Map;

import static org.junit.Assert.assertEquals;

public class InvokeActionRouteTest {
    private static final ObjectMapper JSON_MAPPER = new ObjectMapper();

    private Servient servient;

    @Before
    public void setup() throws ServientException {
        Config config = ConfigFactory
                .parseString("wot.servient.servers = [\"city.sane.wot.binding.http.HttpProtocolServer\"]\n" +
                        "wot.servient.client-factories = []")
                .withFallback(ConfigFactory.load());
        servient = new Servient(config);
        servient.start().join();
    }

    @After
    public void teardown() {
        servient.shutdown().join();
    }

    @Test
    public void invokeAction() throws IOException {
        ExposedThing thing = getCounterThing();
        servient.addThing(thing);
        servient.expose(thing.getId()).join();

        HttpUriRequest request = new HttpPost("http://localhost:8080/counter/actions/increment");
        HttpResponse response = HttpClientBuilder.create().build().execute(request);

        assertEquals(200, response.getStatusLine().getStatusCode());
        assertEquals("application/json", ContentType.getOrDefault(response.getEntity()).getMimeType());
        assertEquals("43", EntityUtils.toString(response.getEntity()));
    }

    @Test
    public void invokeActionWithUrlParameters() throws IOException {
        ExposedThing thing = getCounterThing();
        servient.addThing(thing);
        servient.expose(thing.getId()).join();

        HttpUriRequest request = new HttpPost("http://localhost:8080/counter/actions/increment?step=3");
        HttpResponse response = HttpClientBuilder.create().build().execute(request);

        assertEquals(200, response.getStatusLine().getStatusCode());
        assertEquals("application/json", ContentType.getOrDefault(response.getEntity()).getMimeType());
        assertEquals("45", EntityUtils.toString(response.getEntity()));
    }

    @Test
    public void invokeActionWithEntityParameters() throws IOException, ContentCodecException {
        ExposedThing thing = getCounterThing();
        servient.addThing(thing);
        servient.expose(thing.getId()).join();

        Map<String, Integer> parameters = Map.of("step", 3);
        Content content = ContentManager.valueToContent(parameters, "application/json");

        HttpPost request = new HttpPost("http://localhost:8080/counter/actions/increment");
        request.setHeader("Content-Type", content.getType());
        request.setEntity(new ByteArrayEntity(content.getBody()));
        HttpResponse response = HttpClientBuilder.create().build().execute(request);

        assertEquals(200, response.getStatusLine().getStatusCode());
        assertEquals("application/json", ContentType.getOrDefault(response.getEntity()).getMimeType());
        assertEquals("45", EntityUtils.toString(response.getEntity()));
    }
}