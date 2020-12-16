package city.sane.wot.binding.coap;

import city.sane.wot.content.Content;
import city.sane.wot.thing.form.Form;
import org.eclipse.californium.core.CoapClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.concurrent.ExecutorService;
import java.util.function.Function;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class CoapProtocolClientTest {
    private ExecutorService executor;
    private Function<String, CoapClient> clientCreator;
    private CoapClient coapClient;
    private Form form;
    private Content content;

    @BeforeEach
    public void setUp() throws Exception {
        executor = mock(ExecutorService.class);
        clientCreator = mock(Function.class);
        coapClient = mock(CoapClient.class);
        form = mock(Form.class);
        content = mock(Content.class);
    }

    @Test
    public void readResourceShouldCreateCoapRequest() {
        when(clientCreator.apply(any())).thenReturn(coapClient);

        CoapProtocolClient client = new CoapProtocolClient(clientCreator);
        client.readResource(form);

        verify(coapClient, timeout(1 * 1000L)).advanced(any(), any());
    }

    @Test
    public void writeResourceShouldCreateCoapRequest() {
        when(clientCreator.apply(any())).thenReturn(coapClient);

        CoapProtocolClient client = new CoapProtocolClient(clientCreator);
        client.writeResource(form, content);

        verify(coapClient, timeout(1 * 1000L)).advanced(any(), any());
    }

    @Test
    public void invokeResourceShouldCreateCoapRequest() {
        when(form.getHref()).thenReturn("coap://localhost/counter/actions/reset");
        when(clientCreator.apply(any())).thenReturn(coapClient);

        CoapProtocolClient client = new CoapProtocolClient(clientCreator);
        client.invokeResource(form);

        verify(coapClient, timeout(1 * 1000L)).advanced(any(), any());
    }

    @Test
    public void observeResourceShouldCreateCoapRequest() {
        when(form.getHref()).thenReturn("coap://localhost/counter/properties/count");
        when(clientCreator.apply(any())).thenReturn(coapClient);

        CoapProtocolClient client = new CoapProtocolClient(clientCreator);
        client.observeResource(form).subscribe();

        verify(coapClient, timeout(1 * 1000L)).observe(any());
    }
}