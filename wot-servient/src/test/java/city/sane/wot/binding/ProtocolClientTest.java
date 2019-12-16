package city.sane.wot.binding;

import org.junit.Test;

import java.util.concurrent.ExecutionException;

import static org.junit.Assert.assertFalse;

public class ProtocolClientTest {
    @Test(expected = ProtocolClientNotImplementedException.class)
    public void readResource() throws Throwable {
        try {
            new MyProtocolClient().readResource(null).get();
        }
        catch (ExecutionException e) {
            throw e.getCause();
        }
    }

    @Test(expected = ProtocolClientNotImplementedException.class)
    public void writeResource() throws Throwable {
        try {
            new MyProtocolClient().writeResource(null, null).get();
        }
        catch (ExecutionException e) {
            throw e.getCause();
        }
    }

    @Test(expected = ProtocolClientNotImplementedException.class)
    public void invokeResource() throws Throwable {
        try {
            new MyProtocolClient().invokeResource(null).get();
        }
        catch (ExecutionException e) {
            throw e.getCause();
        }
    }

    @Test(expected = ProtocolClientNotImplementedException.class)
    public void subscribeResource() throws Throwable {
        try {
            new MyProtocolClient().subscribeResource(null, null).get();
        }
        catch (ExecutionException e) {
            throw e.getCause();
        }
    }

    @Test
    public void setSecurity() {
        assertFalse(new MyProtocolClient().setSecurity(null, null));
    }

    @Test(expected = ProtocolClientNotImplementedException.class)
    public void discover() throws Throwable {
        try {
            new MyProtocolClient().discover(null).get();
        }
        catch (ExecutionException e) {
            throw e.getCause();
        }
    }

    class MyProtocolClient implements ProtocolClient {
    }
}