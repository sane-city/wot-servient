package city.sane.wot.binding.coap;

import city.sane.wot.binding.ProtocolClient;
import city.sane.wot.binding.ProtocolClientNotImplementedException;
import city.sane.wot.content.ContentCodecException;
import city.sane.wot.content.ContentManager;
import city.sane.wot.thing.form.Form;
import city.sane.wot.thing.observer.Observer;
import city.sane.wot.thing.observer.Subscription;
import org.eclipse.californium.core.CoapResource;
import org.eclipse.californium.core.CoapServer;
import org.eclipse.californium.core.coap.CoAP;
import org.eclipse.californium.core.coap.MediaTypeRegistry;
import org.eclipse.californium.core.server.resources.CoapExchange;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class CoapProtocolClientIT {
    private CoapProtocolClientFactory clientFactory;
    private ProtocolClient client;
    private CoapServer server;

    @Before
    public void setUp() {
        clientFactory = new CoapProtocolClientFactory();
        clientFactory.init().join();

        client = clientFactory.getClient();

        server = new CoapServer(5683);
        server.add(new MyReadResource());
        server.add(new MyWriteResource());
        server.add(new MyInvokeResource());
        server.add(new MySubscribeResource());
        server.start();
    }

    @After
    public void tearDown() throws TimeoutException {
        clientFactory.destroy().join();
        server.stop();
        CoapProtocolServer.waitForPort(5683);
    }

    @Test
    public void readResource() throws ContentCodecException, ExecutionException, InterruptedException {
        String href = "coap://localhost/read";
        Form form = new Form.Builder().setHref(href).build();

        assertEquals(ContentManager.valueToContent(1337), client.readResource(form).get());
    }

    @Test
    public void writeResource() throws ContentCodecException, ExecutionException, InterruptedException {
        String href = "coap://localhost/write";
        Form form = new Form.Builder().setHref(href).build();

        assertEquals(ContentManager.valueToContent(42), client.writeResource(form, ContentManager.valueToContent(1337)).get());
    }

    @Test
    public void invokeResource() throws ContentCodecException, ExecutionException, InterruptedException {
        String href = "coap://localhost/invoke";
        Form form = new Form.Builder().setHref(href).build();

        assertEquals(ContentManager.valueToContent(42), client.invokeResource(form, ContentManager.valueToContent(1337)).get());
    }

    @Test(timeout = 5 * 1000)
    public void subscribeResource() throws ProtocolClientNotImplementedException, ExecutionException, InterruptedException {
        String href = "coap://localhost/subscribe";
        Form form = new Form.Builder().setHref(href).build();

        CompletableFuture<Void> nextCalledFuture = new CompletableFuture<>();
        assertThat(client.subscribeResource(form, new Observer<>(next -> nextCalledFuture.complete(null))).get(), instanceOf(Subscription.class));

        assertNull(nextCalledFuture.get());
    }

    class MyReadResource extends CoapResource {
        MyReadResource() {
            super("read");
        }

        @Override
        public void handleGET(CoapExchange exchange) {
            int contentFormat = MediaTypeRegistry.parse("application/json");
            exchange.respond(CoAP.ResponseCode.CONTENT, "1337", contentFormat);
        }
    }

    class MyWriteResource extends CoapResource {
        MyWriteResource() {
            super("write");
        }

        @Override
        public void handlePUT(CoapExchange exchange) {
            int contentFormat = MediaTypeRegistry.parse("application/json");
            exchange.respond(CoAP.ResponseCode.CONTENT, "42", contentFormat);
        }
    }

    class MyInvokeResource extends CoapResource {
        MyInvokeResource() {
            super("invoke");
        }

        @Override
        public void handlePOST(CoapExchange exchange) {
            int contentFormat = MediaTypeRegistry.parse("application/json");
            exchange.respond(CoAP.ResponseCode.CONTENT, "42", contentFormat);
        }
    }

    class MySubscribeResource extends CoapResource {
        MySubscribeResource() {
            super("subscribe");

            setObservable(true); // enable observing
            setObserveType(CoAP.Type.CON); // configure the notification type to CONs
            getAttributes().setObservable(); // mark observable in the Link-Format
        }

        @Override
        public void handleGET(CoapExchange exchange) {
            int contentFormat = MediaTypeRegistry.parse("application/json");
            exchange.respond(CoAP.ResponseCode.CONTENT, "42", contentFormat);
        }
    }
}