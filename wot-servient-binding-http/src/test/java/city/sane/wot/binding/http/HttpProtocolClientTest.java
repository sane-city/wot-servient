package city.sane.wot.binding.http;

import city.sane.wot.binding.ProtocolClient;
import city.sane.wot.binding.ProtocolClientNotImplementedException;
import city.sane.wot.content.ContentCodecException;
import city.sane.wot.content.ContentManager;
import city.sane.wot.thing.form.Form;
import city.sane.wot.thing.observer.Observer;
import city.sane.wot.thing.observer.Subscription;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import spark.Request;
import spark.Response;
import spark.Route;
import spark.Service;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class HttpProtocolClientTest {
    private HttpProtocolClientFactory clientFactory;
    private ProtocolClient client;
    private Service service;

    @Before
    public void setUp() {
        clientFactory = new HttpProtocolClientFactory();
        clientFactory.init().join();

        client = clientFactory.getClient();
        service = Service.ignite().ipAddress("127.0.0.1").port(8080);
        service.init();
        service.awaitInitialization();
        service.get("read", new MyReadRoute());
        service.put("write", new MyWriteRoute());
        service.post("invoke", new MyInvokeRoute());
        service.get("subscribe", new MySubscribeRoute());
    }

    @After
    public void tearDown() {
        clientFactory.destroy().join();

        service.stop();
        service.awaitStop();
    }

    @Test
    public void readResource() throws ContentCodecException, ExecutionException, InterruptedException {
        String href = "http://localhost:8080/read";
        Form form = new Form.Builder().setHref(href).build();

        assertEquals(ContentManager.valueToContent(1337), client.readResource(form).get());
    }

    @Test
    public void writeResource() throws ContentCodecException, ExecutionException, InterruptedException {
        String href = "http://localhost:8080/write";
        Form form = new Form.Builder().setHref(href).build();

        assertEquals(ContentManager.valueToContent(42), client.writeResource(form, ContentManager.valueToContent(1337)).get());
    }

    @Test
    public void invokeResource() throws ContentCodecException, ExecutionException, InterruptedException {
        String href = "http://localhost:8080/invoke";
        Form form = new Form.Builder().setHref(href).build();

        assertEquals(ContentManager.valueToContent(42), client.invokeResource(form, ContentManager.valueToContent(1337)).get());
    }

    @Test(timeout = 5 * 1000)
    public void subscribeResource() throws ProtocolClientNotImplementedException, ExecutionException, InterruptedException {
        String href = "http://localhost:8080/subscribe";
        Form form = new Form.Builder().setHref(href).build();

        CompletableFuture<Void> nextCalledFuture = new CompletableFuture<>();
        assertThat(client.subscribeResource(form, new Observer<>(next -> nextCalledFuture.complete(null))).get(), instanceOf(Subscription.class));

        assertNull(nextCalledFuture.get());
    }

    private class MyReadRoute implements Route {
        @Override
        public Object handle(Request request, Response response) {
            response.type("application/json");
            return 1337;
        }
    }

    private class MyWriteRoute implements Route {
        @Override
        public Object handle(Request request, Response response) {
            response.type("application/json");
            return 42;
        }
    }

    private class MyInvokeRoute implements Route {
        @Override
        public Object handle(Request request, Response response) {
            response.type("application/json");
            return 42;
        }
    }

    private class MySubscribeRoute implements Route {
        @Override
        public Object handle(Request request, Response response) {
            response.type("application/json");
            return "Hallo Welt";
        }
    }
}