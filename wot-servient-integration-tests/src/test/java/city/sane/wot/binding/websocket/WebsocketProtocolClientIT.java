package city.sane.wot.binding.websocket;

import city.sane.wot.binding.ProtocolClient;
import city.sane.wot.binding.coap.CoapProtocolClientFactory;
import city.sane.wot.content.ContentCodecException;
import city.sane.wot.content.ContentManager;
import city.sane.wot.thing.form.Form;
import com.typesafe.config.ConfigFactory;
import org.eclipse.californium.core.CoapServer;
import org.java_websocket.server.WebSocketServer;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.concurrent.ExecutionException;

import static org.junit.Assert.*;

public class WebsocketProtocolClientIT {
    private WebsocketProtocolClientFactory clientFactory;
    private ProtocolClient client;
    private WebSocketServer server;

    @Before
    public void setUp() {
        clientFactory = new WebsocketProtocolClientFactory(ConfigFactory.load());
        clientFactory.init().join();

        client = clientFactory.getClient();

        // FIXME: start dummy websocket server here
    }

    @After
    public void tearDown() {
        clientFactory.destroy().join();

        // FIXME: stop dummy websocket server here
    }

    @Test
    public void readResource() throws ContentCodecException, ExecutionException, InterruptedException {
        String href = "ws://localhost:8080";
        Form form = new Form.Builder().setHref(href).build();

        assertEquals(ContentManager.valueToContent(1337), client.readResource(form).get());
    }

    @Test
    public void writeResource() throws ContentCodecException, ExecutionException, InterruptedException {
        String href = "ws://localhost:8080";
        Form form = new Form.Builder().setHref(href).build();

        assertEquals(ContentManager.valueToContent(42), client.writeResource(form, ContentManager.valueToContent(1337)).get());
    }

    @Test
    public void invokeResource() throws ContentCodecException, ExecutionException, InterruptedException {
        String href = "ws://localhost:8080";
        Form form = new Form.Builder().setHref(href).build();

        assertEquals(ContentManager.valueToContent(42), client.invokeResource(form, ContentManager.valueToContent(1337)).get());
    }
}