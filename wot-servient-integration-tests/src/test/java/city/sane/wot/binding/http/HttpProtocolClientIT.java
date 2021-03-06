/*
 * Copyright (c) 2021.
 *
 * This file is part of SANE Web of Things Servient.
 *
 * SANE Web of Things Servient is free software: you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * SANE Web of Things Servient is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with SANE Web of Things Servient.  If not, see
 * <http://www.gnu.org/licenses/>.
 */
package city.sane.wot.binding.http;

import city.sane.wot.binding.ProtocolClient;
import city.sane.wot.binding.ProtocolClientException;
import city.sane.wot.content.ContentCodecException;
import city.sane.wot.content.ContentManager;
import city.sane.wot.thing.form.Form;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import spark.Request;
import spark.Response;
import spark.Route;
import spark.Service;

import java.util.concurrent.ExecutionException;

import static java.util.concurrent.TimeUnit.SECONDS;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class HttpProtocolClientIT {
    private HttpProtocolClientFactory clientFactory;
    private ProtocolClient client;
    private Service service;

    @BeforeEach
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
        service.get("status/300", new MyStatusRoute());
        service.get("status/400", new MyStatusRoute());
        service.get("status/500", new MyStatusRoute());
    }

    @AfterEach
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
    public void readResourceRedirection() {
        String href = "http://localhost:8080/status/300";
        Form form = new Form.Builder().setHref(href).build();

        assertThrows(ProtocolClientException.class, () -> {
            try {
                client.readResource(form).get();
            }
            catch (ExecutionException e) {
                throw e.getCause();
            }
        });
    }

    @Test
    public void readResourceClientError() {
        String href = "http://localhost:8080/status/400";
        Form form = new Form.Builder().setHref(href).build();

        assertThrows(ProtocolClientException.class, () -> {
            try {
                client.readResource(form).get();
            }
            catch (ExecutionException e) {
                throw e.getCause();
            }
        });
    }

    @Test
    public void readResourceServerError() {
        String href = "http://localhost:8080/status/500";
        Form form = new Form.Builder().setHref(href).build();

        assertThrows(ProtocolClientException.class, () -> {
            try {
                client.readResource(form).get();
            }
            catch (ExecutionException e) {
                throw e.getCause();
            }
        });
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

    @Test
    @Timeout(value = 5, unit = SECONDS)
    public void subscribeResource() throws ProtocolClientException, ExecutionException, InterruptedException, ContentCodecException {
        String href = "http://localhost:8080/subscribe";
        Form form = new Form.Builder().setHref(href).build();

        assertEquals(
                ContentManager.valueToContent(1337),
                client.observeResource(form).firstElement().blockingGet()
        );
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
            return 1337;
        }
    }

    private class MyStatusRoute implements Route {
        @Override
        public Object handle(Request request, Response response) throws Exception {
            int statusCode = Integer.parseInt(request.pathInfo().split("/", 3)[2]);
            response.status(statusCode);
            return "";
        }
    }
}