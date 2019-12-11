package city.sane.wot.binding.file;

import city.sane.wot.content.Content;
import city.sane.wot.thing.form.Form;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.ExecutionException;

import static org.junit.Assert.assertNotNull;

public class FileProtocolClientTest {
    @Rule
    public final TemporaryFolder thingDirectory = new TemporaryFolder();
    private File thing;

    @Before
    public void setup() throws IOException {
        thing = thingDirectory.newFile("ThingA.json");
    }

//    @Test
//    public void readResourceRelativeUrl() throws ExecutionException, InterruptedException {
//        FileProtocolClient client = new FileProtocolClient();
//        String href = "file:../thingDescriptionExamples/cf-sandbox.jsonld";
//        Form form = new Form.Builder().setHref(href).build();
//
//        Content content = client.readResource(form).get();
//        assertNotNull(content);
//    }

    @Test
    public void readResourceAbsoluteUrl() throws ExecutionException, InterruptedException {
        FileProtocolClient client = new FileProtocolClient();
        String href = "file:" + thing.getPath();
        Form form = new Form.Builder()
                .setHref(href)
                .build();

        Content content = client.readResource(form).get();
        assertNotNull(content);
    }
}