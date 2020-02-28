package city.sane.wot.binding.websocket;

import city.sane.wot.binding.ProtocolClientException;
import city.sane.wot.binding.websocket.WebsocketProtocolClient.WebsocketClient;
import city.sane.wot.binding.websocket.message.*;
import city.sane.wot.content.Content;
import city.sane.wot.thing.form.Form;
import org.junit.Before;
import org.junit.Test;

import java.net.URI;
import java.util.Map;
import java.util.function.Consumer;

import static city.sane.wot.binding.websocket.WebsocketProtocolServer.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class WebsocketProtocolClientTest {
    private Map<URI, WebsocketClient> clients;
    private Map<String, Consumer<AbstractServerMessage>> openRequests;
    private Form form;
    private WebsocketClient websocketClient;
    private Content content;

    @Before
    public void setUp() {
        clients = mock(Map.class);
        openRequests = mock(Map.class);
        form = mock(Form.class);
        websocketClient = mock(WebsocketClient.class);
        content = mock(Content.class);
    }

    @Test
    public void readResourceShouldSendMessageGivenInFormToSocket() {
        when(form.getOptional(any())).thenReturn(Map.of(
                WEBSOCKET_MESSAGE_TYPE, "ReadProperty",
                WEBSOCKET_MESSAGE_THING_ID, "counter",
                WEBSOCKET_MESSAGE_NAME, "count"
        ));
        when(form.getHref()).thenReturn("wss://localhost");
        when(clients.get(any())).thenReturn(websocketClient);
        when(websocketClient.isOpen()).thenReturn(true);

        WebsocketProtocolClient client = new WebsocketProtocolClient(clients, openRequests);
        client.readResource(form);

        verify(websocketClient, timeout(1 * 1000L).times(1)).send(any(ReadProperty.class));
    }

    @Test
    public void writeResourceShouldSendMessageGivenInFormToSocket() {
        when(form.getOptional(any())).thenReturn(Map.of(
                WEBSOCKET_MESSAGE_TYPE, "WriteProperty",
                WEBSOCKET_MESSAGE_THING_ID, "counter",
                WEBSOCKET_MESSAGE_NAME, "count"
        ));
        when(form.getHref()).thenReturn("wss://localhost");
        when(clients.get(any())).thenReturn(websocketClient);
        when(websocketClient.isOpen()).thenReturn(true);

        WebsocketProtocolClient client = new WebsocketProtocolClient(clients, openRequests);
        client.writeResource(form, content);

        verify(websocketClient, timeout(1 * 1000L).times(1)).send(any(WriteProperty.class));
    }

    @Test
    public void invokeResourceShouldSendMessageGivenInFormToSocket() {
        when(form.getOptional(any())).thenReturn(Map.of(
                WEBSOCKET_MESSAGE_TYPE, "InvokeAction",
                WEBSOCKET_MESSAGE_THING_ID, "counter",
                WEBSOCKET_MESSAGE_NAME, "reset"
        ));
        when(form.getHref()).thenReturn("wss://localhost");
        when(clients.get(any())).thenReturn(websocketClient);
        when(websocketClient.isOpen()).thenReturn(true);

        WebsocketProtocolClient client = new WebsocketProtocolClient(clients, openRequests);
        client.invokeResource(form);

        verify(websocketClient, timeout(1 * 1000L).times(1)).send(any(InvokeAction.class));
    }

    @Test
    public void observeResourceShouldSendMessageGivenInFormToSocket() throws ProtocolClientException {
        when(form.getOptional(any())).thenReturn(Map.of(
                WEBSOCKET_MESSAGE_TYPE, "SubscribeProperty",
                WEBSOCKET_MESSAGE_THING_ID, "counter",
                WEBSOCKET_MESSAGE_NAME, "count"
        ));
        when(form.getHref()).thenReturn("wss://localhost");
        when(clients.get(any())).thenReturn(websocketClient);
        when(websocketClient.isOpen()).thenReturn(true);

        WebsocketProtocolClient client = new WebsocketProtocolClient(clients, openRequests);
        client.observeResource(form).subscribe();

        verify(websocketClient, timeout(1 * 1000L).times(1)).send(any(SubscribeProperty.class));
    }
}