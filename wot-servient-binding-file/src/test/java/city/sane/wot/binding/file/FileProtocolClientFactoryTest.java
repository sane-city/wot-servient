package city.sane.wot.binding.file;

import org.junit.jupiter.api.Test;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertEquals;

public class FileProtocolClientFactoryTest {
    @Test
    public void getScheme() {
        assertEquals("file", new FileProtocolClientFactory().getScheme());
    }

    @Test
    public void getClient() {
        assertThat(new FileProtocolClientFactory().getClient(), instanceOf(FileProtocolClient.class));
    }
}