package city.sane.wot.binding;

import city.sane.wot.content.Content;
import city.sane.wot.thing.filter.ThingFilter;
import city.sane.wot.thing.form.Form;
import io.reactivex.rxjava3.core.Observer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.concurrent.ExecutionException;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
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

    @BeforeEach
    public void setUp() {
        form = mock(Form.class);
        content = mock(Content.class);
        observer = mock(Observer.class);
        metadata = mock(List.class);
        credentials = mock(Object.class);
        filter = mock(ThingFilter.class);
        client = spy(ProtocolClient.class);
    }

    @Test
    public void readResourceShouldThrowProtocolClientNotImplementedException() {
        assertThrows(ProtocolClientNotImplementedException.class, () -> {
            try {
                client.readResource(form).get();
            }
            catch (ExecutionException e) {
                throw e.getCause();
            }
        });
    }

    @Test
    public void writeResourceShouldThrowProtocolClientNotImplementedException() {
        assertThrows(ProtocolClientNotImplementedException.class, () -> {
            try {
                client.writeResource(form, content).get();
            }
            catch (ExecutionException e) {
                throw e.getCause();
            }
        });
    }

    @Test
    public void invokeResourceShouldThrowProtocolClientNotImplementedException() {
        assertThrows(ProtocolClientNotImplementedException.class, () -> {
            try {
                client.invokeResource(form).get();
            }
            catch (ExecutionException e) {
                throw e.getCause();
            }
        });
    }

    @Test
    public void subscribeResourceShouldThrowProtocolClientNotImplementedException() {
        assertThrows(ProtocolClientNotImplementedException.class, () -> {
            client.observeResource(form).subscribe(observer);
        });
    }

    @Test
    public void setSecurityShouldReturnFalse() {
        assertFalse(client.setSecurity(metadata, credentials));
    }

    @Test
    public void discoverShouldThrowProtocolClientNotImplementedException() {
        assertThrows(ProtocolClientNotImplementedException.class, () -> {
            client.discover(filter);
        });
    }
}
