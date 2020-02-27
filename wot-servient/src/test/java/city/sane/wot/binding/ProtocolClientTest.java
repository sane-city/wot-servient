package city.sane.wot.binding;

import city.sane.wot.content.Content;
import city.sane.wot.thing.filter.ThingFilter;
import city.sane.wot.thing.form.Form;
import io.reactivex.rxjava3.core.Observer;
import org.junit.Before;
import org.junit.Test;

import java.util.List;
import java.util.concurrent.ExecutionException;

import static org.junit.Assert.assertFalse;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;

public class ProtocolClientTest {
    private Form form;
    private Content content;
    private Observer observer;
    private List metadata;
    private Object credentials;
    private ThingFilter filter;
    private ProtocolClient client;

    @Before
    public void setUp() {
        form = mock(Form.class);
        content = mock(Content.class);
        observer = mock(Observer.class);
        metadata = mock(List.class);
        credentials = mock(Object.class);
        filter = mock(ThingFilter.class);
        client = spy(ProtocolClient.class);
    }

    @Test(expected = ProtocolClientNotImplementedException.class)
    public void readResourceShouldThrowProtocolClientNotImplementedException() throws Throwable {
        try {
            client.readResource(form).get();
        }
        catch (ExecutionException e) {
            throw e.getCause();
        }
    }

    @Test(expected = ProtocolClientNotImplementedException.class)
    public void writeResourceShouldThrowProtocolClientNotImplementedException() throws Throwable {
        try {
            client.writeResource(form, content).get();
        }
        catch (ExecutionException e) {
            throw e.getCause();
        }
    }

    @Test(expected = ProtocolClientNotImplementedException.class)
    public void invokeResourceShouldThrowProtocolClientNotImplementedException() throws Throwable {
        try {
            client.invokeResource(form).get();
        }
        catch (ExecutionException e) {
            throw e.getCause();
        }
    }

    @Test(expected = ProtocolClientNotImplementedException.class)
    public void subscribeResourceShouldThrowProtocolClientNotImplementedException() throws Throwable {
        client.observeResource(form).subscribe(observer);
    }

    @Test
    public void setSecurityShouldReturnFalse() {
        assertFalse(client.setSecurity(metadata, credentials));
    }

    @Test(expected = ProtocolClientNotImplementedException.class)
    public void discoverShouldThrowProtocolClientNotImplementedException() throws ProtocolClientNotImplementedException {
        client.discover(filter);
    }
}
