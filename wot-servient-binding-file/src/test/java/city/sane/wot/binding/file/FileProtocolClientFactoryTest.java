package city.sane.wot.binding.file;

import org.junit.Test;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertEquals;

public class FileProtocolClientFactoryTest {
    @Test
    public void getScheme() {
        assertEquals("file", new FileProtocolClientFactory(null).getScheme());
    }

    @Test
    public void getClient() {
        assertThat(new FileProtocolClientFactory(null).getClient(), instanceOf(FileProtocolClient.class));
    }
}