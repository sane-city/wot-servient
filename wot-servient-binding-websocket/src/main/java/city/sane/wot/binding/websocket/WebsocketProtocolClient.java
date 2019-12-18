package city.sane.wot.binding.websocket;

import city.sane.wot.binding.ProtocolClient;
import city.sane.wot.binding.ProtocolClientException;
import city.sane.wot.binding.websocket.message.*;
import city.sane.wot.content.Content;
import city.sane.wot.thing.form.Form;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.ExecutionException;

/**
 * TODO: Currently a WebsocketProtocolClient and therefore also a WebSocketClient is created for each Thing. Even if each thing is reachable via the same socket. It would be better if there was only one WebSocketClient per socket and that is shared by all WebsocketProtocolClient instanceis.
 * TODO: WebSocketClient.close() is never called!
 */
public class WebsocketProtocolClient implements ProtocolClient {
    private final static Logger log = LoggerFactory.getLogger(WebsocketProtocolClient.class);
    private static ObjectMapper JSON_MAPPER = new ObjectMapper();
    private final Map<URI, WebSocketClient> clients = new HashMap<>();
    CompletableFuture<Content> future = new CompletableFuture<>();

    @Override
    public CompletableFuture<Content> readResource(Form form) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                String json = JSON_MAPPER.writeValueAsString(form);
                // TODO: need return value for test
                getClientFor(form).get().send(json);
                return future.get();
            } catch (JsonProcessingException | InterruptedException | ExecutionException | ProtocolClientException e) {
                throw new CompletionException(e);
            }
        });
    }

    private synchronized CompletableFuture<WebSocketClient> getClientFor(Form form) throws ProtocolClientException {
        try {
            URI uri = new URI(form.getHref());
            WebSocketClient client = clients.get(uri);
            if (client == null || !client.isOpen()) {
                log.info("Create new websocket client for socket '{}'", uri);
                CompletableFuture<WebSocketClient> result = new CompletableFuture<>();

                client = new WebSocketClient(uri) {
                    @Override
                    public void onOpen(ServerHandshake serverHandshake) {
                        log.debug("Websocket to '{}' is ready", uri);
                        clients.put(uri, this);
                        result.complete(this);
                    }

                    @Override
                    public void onMessage(String json) {
                        log.debug("Received new message on websocket to '{}': {}", uri, json);
                        try {
                            AbstractServerMessage message = JSON_MAPPER.readValue(json, AbstractServerMessage.class);
                            if (message instanceof ReadPropertyResponse) {
                                // TODO: need to something here?
                                ReadPropertyResponse rMessage = (ReadPropertyResponse) message;
                                future.complete((Content) rMessage.getValue());
                                System.out.println("ReadPropertyResponse");
                            } else if (message instanceof WritePropertyResponse) {
                                // TODO: need to something here?
                                System.out.println("WritePropertyResponse");
                            }
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }

                    @Override
                    public void onClose(int i, String s, boolean b) {
                        log.debug("Websocket to '{}' is closed", uri);
                        clients.remove(uri);
                    }

                    @Override
                    public void onError(Exception e) {
                        log.warn("An error occured on websocket to '{}': ", uri, e.getMessage());
                        result.completeExceptionally(new ProtocolClientException(e));
                    }
                };
                client.connect();

                return result;
            }
            else {
                return CompletableFuture.completedFuture(client);
            }
        } catch (URISyntaxException e) {
            throw new ProtocolClientException("Unable to create websocket client for href '" + form.getHref() + "': " + e.getMessage());
        }
    }

    @Override
    public CompletableFuture<Content> writeResource(Form form, Content content) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                Form writeForm = new Form.Builder(form).setOptional("payload", content).build();
                String json = JSON_MAPPER.writeValueAsString(writeForm);
                getClientFor(form).get().send(json);

                // TODO:
                return null;
            } catch (JsonProcessingException | ProtocolClientException | InterruptedException | ExecutionException e) {
                throw new CompletionException(e);
            }
        });
    }

    @Override
    public CompletableFuture<Content> invokeResource(Form form, Content content) {
        // FIXME: Implement
        return CompletableFuture.completedFuture(null);
    }
}
