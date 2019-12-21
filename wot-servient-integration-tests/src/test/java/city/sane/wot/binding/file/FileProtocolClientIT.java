package city.sane.wot.binding.file;

import city.sane.wot.content.Content;
import city.sane.wot.thing.form.Form;
import city.sane.wot.thing.observer.Observer;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import static org.junit.Assert.assertEquals;

public class FileProtocolClientIT {
    @Rule
    public final TemporaryFolder thingDirectory = new TemporaryFolder();
    private File thing;

    @Before
    public void setup() throws IOException {
        thing = thingDirectory.newFile("ThingA.json");
        Files.writeString(thing.toPath(), "{\"id\":\"counter\",\"name\":\"Counter\"}", StandardOpenOption.CREATE);
    }

    @Test
    public void readResourceAbsoluteUrl() throws ExecutionException, InterruptedException {
        FileProtocolClient client = new FileProtocolClient();
        String href = "file:" + thing.getPath();
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
        String href = "file:" + thing.getPath();
        Form form = new Form.Builder()
                .setHref(href)
                .build();

        Content content = new Content("application/json", "{\"id\":\"counter\",\"name\":\"Zähler\"}".getBytes());
        client.writeResource(form, content).get();

        assertEquals("{\"id\":\"counter\",\"name\":\"Zähler\"}", Files.readString(thing.toPath()));
    }

    @Test(timeout = 20 * 1000L)
    public void subscribeResourceFileChanged() throws ExecutionException, InterruptedException, IOException {
        FileProtocolClient client = new FileProtocolClient();
        String href = "file:" + thing.getPath();
        Form form = new Form.Builder()
                .setHref(href)
                .build();

        CompletableFuture<Content> future = new CompletableFuture<>();
        Observer<Content> observer = new Observer<>(future::complete);

        client.subscribeResource(form, observer).get();

        // wait until client establish subscription
        // TODO: This is error-prone. We need a feature that notifies us when the subscription is active.
        Thread.sleep(1 * 1000L);

        Files.writeString(thing.toPath(), "{\"id\":\"counter\",\"name\":\"Zähler\"}", StandardOpenOption.CREATE);

        assertEquals("{\"id\":\"counter\",\"name\":\"Zähler\"}", new String(future.get().getBody()));
    }

    @Test(timeout = 20 * 1000L)
    public void subscribeResourceFileCreated() throws ExecutionException, InterruptedException, IOException {
        FileProtocolClient client = new FileProtocolClient();
        String href = "file:" + thing.getPath();
        Form form = new Form.Builder()
                .setHref(href)
                .build();

        CompletableFuture<Content> future = new CompletableFuture<>();
        Observer<Content> observer = new Observer<>(future::complete);

        thing.delete();

        client.subscribeResource(form, observer).get();

        // wait until client establish subscription
        // TODO: This is error-prone. We need a feature that notifies us when the subscription is active.
        Thread.sleep(1 * 1000L);

        Files.writeString(thing.toPath(), "{\"id\":\"counter\",\"name\":\"Zähler\"}", StandardOpenOption.CREATE);

        assertEquals("{\"id\":\"counter\",\"name\":\"Zähler\"}", new String(future.get().getBody()));
    }

    @Test(timeout = 20 * 1000L)
    public void subscribeResourceFileDeleted() throws ExecutionException, InterruptedException, IOException {
        FileProtocolClient client = new FileProtocolClient();
        String href = "file:" + thing.getPath();
        Form form = new Form.Builder()
                .setHref(href)
                .build();

        CompletableFuture<Content> future = new CompletableFuture<>();
        Observer<Content> observer = new Observer<>(future::complete);

        client.subscribeResource(form, observer).get();

        // wait until client establish subscription
        // TODO: This is error-prone. We need a feature that notifies us when the subscription is active.
        Thread.sleep(1 * 1000L);

        thing.delete();

        assertEquals(Content.EMPTY_CONTENT, future.get());
    }
}