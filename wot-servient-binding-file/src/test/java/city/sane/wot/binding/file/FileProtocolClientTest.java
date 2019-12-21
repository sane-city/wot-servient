package city.sane.wot.binding.file;

import city.sane.wot.content.Content;
import city.sane.wot.thing.form.Form;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.util.concurrent.ExecutionException;

import static org.junit.Assert.assertEquals;

public class FileProtocolClientTest {
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
}