package city.sane.wot.binding;

import org.junit.Test;

import java.util.concurrent.ExecutionException;

import static org.junit.Assert.assertNull;

public class ProtocolClientFactoryTest {
    @Test
    public void init() throws ExecutionException, InterruptedException {
        assertNull(new MyProtocolClientFactory().init().get());
    }

    @Test
    public void destroy() throws ExecutionException, InterruptedException {
        assertNull(new MyProtocolClientFactory().destroy().get());
    }

    class MyProtocolClientFactory implements ProtocolClientFactory {
        @Override
        public String getScheme() {
            return null;
        }

        @Override
        public ProtocolClient getClient() {
            return null;
        }
    }
}