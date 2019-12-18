package city.sane.wot.binding.websocket;

import city.sane.wot.binding.ProtocolClient;
import city.sane.wot.binding.coap.CoapProtocolClientFactory;
import city.sane.wot.content.ContentCodecException;
import city.sane.wot.content.ContentManager;
import city.sane.wot.thing.form.Form;
import com.typesafe.config.ConfigFactory;
import org.eclipse.californium.core.CoapServer;
import org.java_websocket.WebSocket;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.server.WebSocketServer;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.concurrent.ExecutionException;

import static org.junit.Assert.*;

public class WebsocketProtocolClientIT {
    private WebsocketProtocolClientFactory clientFactory;
    private ProtocolClient client;
    private WebSocketServer server;

    @Before
    public void setUp() throws InterruptedException {
        clientFactory = new WebsocketProtocolClientFactory();
        clientFactory.init().join();

        client = clientFactory.getClient();

        server = new MyWebSocketServer(new InetSocketAddress(8080));
        server.start();

        Thread.sleep(1 * 1000L);
    }

    @After
    public void tearDown() throws IOException, InterruptedException {
        clientFactory.destroy().join();
        server.stop();
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

    private static class MyWebSocketServer extends WebSocketServer {
        public MyWebSocketServer(InetSocketAddress address) {
            super(address);
        }

        @Override
        public void onOpen(WebSocket conn, ClientHandshake handshake) {

        }

        @Override
        public void onClose(WebSocket conn, int code, String reason, boolean remote) {

        }

        @Override
        public void onMessage(WebSocket conn, String message) {

        }

        @Override
        public void onError(WebSocket conn, Exception ex) {
            throw new RuntimeException(ex);
        }

        @Override
        public void onStart() {

        }
    }
}