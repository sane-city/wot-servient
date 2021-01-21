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
package city.sane.wot.binding.file;

import city.sane.wot.content.Content;
import city.sane.wot.thing.form.Form;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.time.Duration;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicInteger;

import static org.awaitility.Awaitility.await;
import static org.awaitility.Awaitility.fieldIn;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class FileProtocolClientIT {
    private Path thing;

    @BeforeEach
    public void setup(@TempDir Path thingDirectory) throws IOException {
        thing = Paths.get(thingDirectory.toString(), "ThingA.json");
        Files.writeString(thing, "{\"id\":\"counter\",\"name\":\"Counter\"}", StandardOpenOption.CREATE);
    }

    @Test
    public void readResourceAbsoluteUrl() throws ExecutionException, InterruptedException {
        FileProtocolClient client = new FileProtocolClient();
        String href = thing.toUri().toString();
        Form form = new Form.Builder()
                .setHref(href)
                .build();

        Content content = client.readResource(form).get();

        assertEquals("application/json", content.getType());
        assertEquals("{\"id\":\"counter\",\"name\":\"Counter\"}", new String(content.getBody()));
    }

    @Test
    public void writeResourceAbsoluteUrl() throws ExecutionException, InterruptedException, IOException {
        FileProtocolClient client = new FileProtocolClient();
        String href = thing.toUri().toString();
        Form form = new Form.Builder()
                .setHref(href)
                .build();

        Content content = new Content("application/json", "{\"id\":\"counter\",\"name\":\"Zähler\"}".getBytes());
        client.writeResource(form, content).get();

        assertEquals("{\"id\":\"counter\",\"name\":\"Zähler\"}", Files.readString(thing));
    }

    @Test
    public void subscribeResourceFileChanged() throws ExecutionException, InterruptedException, IOException, TimeoutException {
        FileProtocolClient client = new FileProtocolClient();
        String href = thing.toUri().toString();
        Form form = new Form.Builder()
                .setHref(href)
                .build();

        Future<Content> future = client.observeResource(form).firstElement().toFuture();

        // wait until client has established subscription
        await().atMost(Duration.ofSeconds(10))
                .untilAtomic(
                        fieldIn(client).ofType(AtomicInteger.class).andWithName("subscriptionsCount").call(),
                        equalTo(1)
                );

        Files.writeString(thing, "{\"id\":\"counter\",\"name\":\"Zähler\"}", StandardOpenOption.CREATE);

        assertEquals("{\"id\":\"counter\",\"name\":\"Zähler\"}", new String(future.get(10, TimeUnit.SECONDS).getBody()));
    }

    @Test
    public void subscribeResourceFileCreated() throws ExecutionException, InterruptedException, IOException, TimeoutException {
        FileProtocolClient client = new FileProtocolClient();
        String href = thing.toUri().toString();
        Form form = new Form.Builder()
                .setHref(href)
                .build();

        thing.toFile().delete();

        Future<Content> future = client.observeResource(form).firstElement().toFuture();

        // wait until client has established subscription
        await().atMost(Duration.ofSeconds(10))
                .untilAtomic(
                        fieldIn(client).ofType(AtomicInteger.class).andWithName("subscriptionsCount").call(),
                        equalTo(1)
                );

        Files.writeString(thing, "{\"id\":\"counter\",\"name\":\"Zähler\"}", StandardOpenOption.CREATE);

        assertEquals("{\"id\":\"counter\",\"name\":\"Zähler\"}", new String(future.get(20, TimeUnit.SECONDS).getBody()));
    }

    @Test
    public void subscribeResourceFileDeleted() throws ExecutionException, InterruptedException, TimeoutException {
        FileProtocolClient client = new FileProtocolClient();
        String href = thing.toUri().toString();
        Form form = new Form.Builder()
                .setHref(href)
                .build();

        Future<Content> future = client.observeResource(form).firstElement().toFuture();

        // wait until client has established subscription
        await().atMost(Duration.ofSeconds(10))
                .untilAtomic(
                        fieldIn(client).ofType(AtomicInteger.class).andWithName("subscriptionsCount").call(),
                        equalTo(1)
                );

        thing.toFile().delete();

        assertEquals(Content.EMPTY_CONTENT, future.get(20, TimeUnit.SECONDS));
    }
}