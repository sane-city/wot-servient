package city.sane.wot.binding;

import city.sane.wot.content.Content;
import city.sane.wot.thing.filter.ThingFilter;
import city.sane.wot.thing.form.Form;
import city.sane.wot.thing.observer.Observer;
import org.junit.Before;
import org.junit.Test;

import java.util.List;
import java.util.concurrent.ExecutionException;

import static org.junit.Assert.assertFalse;
import static org.mockito.Mockito.mock;

public class ProtocolClientTest {
    private Form form;
    private Content content;
    private Observer observer;
    private List metadata;
    private Object credentials;
    private ThingFilter filter;

    @Before
    public void setUp() {
        form = mock(Form.class);
        content = mock(Content.class);
        observer = mock(Observer.class);
        metadata = mock(List.class);
        credentials = mock(Object.class);
        filter = mock(ThingFilter.class);
    }

    @Test(expected = ProtocolClientNotImplementedException.class)
    public void readResource() throws Throwable {
        try {
            new MyProtocolClient().readResource(form).get();
        }
        catch (ExecutionException e) {
            throw e.getCause();
        }
    }

    @Test(expected = ProtocolClientNotImplementedException.class)
    public void writeResource() throws Throwable {
        try {
            new MyProtocolClient().writeResource(form, content).get();
        }
        catch (ExecutionException e) {
            throw e.getCause();
        }
    }

    @Test(expected = ProtocolClientNotImplementedException.class)
    public void invokeResource() throws Throwable {
        try {
            new MyProtocolClient().invokeResource(form).get();
        }
        catch (ExecutionException e) {
            throw e.getCause();
        }
    }

    @Test(expected = ProtocolClientNotImplementedException.class)
    public void subscribeResource() throws Throwable {
        try {
            new MyProtocolClient().subscribeResource(form, observer).get();
        }
        catch (ExecutionException e) {
            throw e.getCause();
        }
    }

    @Test
    public void setSecurity() {
        assertFalse(new MyProtocolClient().setSecurity(metadata, credentials));
    }

    @Test(expected = ProtocolClientNotImplementedException.class)
    public void discover() throws Throwable {
        try {
            new MyProtocolClient().discover(filter).get();
        }
        catch (ExecutionException e) {
            throw e.getCause();
        }
    }

    class MyProtocolClient implements ProtocolClient {
    }
}