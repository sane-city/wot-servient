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
import java.util.concurrent.ExecutionException;
import java.util.function.Consumer;

/**
 * TODO: Currently a WebsocketProtocolClient and therefore also a WebSocketClient is created for each Thing. Even if each thing is reachable via the same socket. It would be better if there was only one WebSocketClient per socket and that is shared by all WebsocketProtocolClient instanceis.
 * TODO: WebSocketClient.close() is never called!
 */
public class WebsocketProtocolClient implements ProtocolClient {
    private final static Logger log = LoggerFactory.getLogger(WebsocketProtocolClient.class);

    private static ObjectMapper JSON_MAPPER = new ObjectMapper();
    private final Map<URI, WebSocketClient> clients = new HashMap<>();
    private final Map<String, Consumer<AbstractServerMessage>> openRequests = new HashMap<>();

    @Override
    public CompletableFuture<Content> readResource(Form form) {
        log.debug("Read resource: {}", form);
        return sendMessage(form);
    }

    @Override
    public CompletableFuture<Content> writeResource(Form form, Content content) {
        log.debug("Write resource '{}' with content '{}'", form, content);
        return sendMessageWithContent(form, content);
    }


    @Override
    public CompletableFuture<Content> invokeResource(Form form, Content content) {
        log.debug("Invoke resource '{}' with content '{}'", form, content);
        return sendMessageWithContent(form, content);
    }

    private CompletableFuture<Content> sendMessage(Form form) {
        Object message = form.getOptional("websocket:message");
        if (message != null) {
            try {
                AbstractClientMessage clientMessage = JSON_MAPPER.convertValue(message, AbstractClientMessage.class);

                try {
                    WebSocketClient client = getClientFor(form).get();
                    AbstractServerMessage response = ask(client, clientMessage).get();
                    return CompletableFuture.completedFuture(response.toContent());
                }
                catch (InterruptedException | ExecutionException e) {
                    return CompletableFuture.failedFuture(e);
                }
            }
            catch (IllegalArgumentException e) {
                return CompletableFuture.failedFuture(new ProtocolClientException("Client is unable to parse given message: " + e.getMessage()));
            }
            catch (ProtocolClientException e) {
                return CompletableFuture.failedFuture(e);
            }
        }
        else {
            return CompletableFuture.failedFuture(new ProtocolClientException("Client does not know what message should be written to the socket."));
        }
    }

    private CompletableFuture<Content> sendMessageWithContent(Form form, Content content) {
        Object message = form.getOptional("websocket:message");
        if (message != null) {
            try {
                ThingInteractionWithContent clientMessage = JSON_MAPPER.convertValue(message, ThingInteractionWithContent.class);
                clientMessage.setValue(content);

                try {
                    WebSocketClient client = getClientFor(form).get();
                    AbstractServerMessage response = ask(client, clientMessage).get();
                    return CompletableFuture.completedFuture(response.toContent());
                }
                catch (InterruptedException | ExecutionException e) {
                    return CompletableFuture.failedFuture(e);
                }
            }
            catch (IllegalArgumentException e) {
                return CompletableFuture.failedFuture(new ProtocolClientException("Client is unable to parse given message: " + e.getMessage()));
            }
            catch (ProtocolClientException e) {
                return CompletableFuture.failedFuture(e);
            }
        }
        else {
            return CompletableFuture.failedFuture(new ProtocolClientException("Client does not know what message should be written to the socket."));
        }
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
                        log.debug("Websocket client for socket '{}' is ready", uri);
                        clients.put(uri, this);
                        result.complete(this);
                    }

                    @Override
                    public void onMessage(String json) {
                        log.debug("Received message on websocket client for socket '{}': {}", uri, json);
                        try {
                            AbstractServerMessage message = JSON_MAPPER.readValue(json, AbstractServerMessage.class);

                            if (message != null) {
                                Consumer<AbstractServerMessage> openRequest = openRequests.get(message.getId());

                                if (openRequest != null) {
                                    log.debug("Found open request. Accept");
                                    openRequest.accept(message);
                                }
                                else {
                                    log.warn("Unexpected response. Discard!");
                                }
                            }
                            else {
                                log.warn("Message is null. Discard!");
                            }
                        }
                        catch (IOException e) {
                            e.printStackTrace();
                        }
                    }

                    @Override
                    public void onClose(int i, String s, boolean b) {
                        log.debug("Websocket client for socket '{}' is closed", uri);
                        clients.remove(uri);
                    }

                    @Override
                    public void onError(Exception e) {
                        log.warn("An error occured on websocket client for socket '{}': ", uri, e.getMessage());
                        result.completeExceptionally(new ProtocolClientException(e));
                    }
                };
                client.connect();

                return result;
            }
            else {
                return CompletableFuture.completedFuture(client);
            }
        }
        catch (URISyntaxException e) {
            throw new ProtocolClientException("Unable to create websocket client for href '" + form.getHref() + "': " + e.getMessage());
        }
    }

    private CompletableFuture<AbstractServerMessage> ask(WebSocketClient client, AbstractClientMessage request) {
        log.debug("Websocket client for socket '{}' is sending message: {}", client.getURI(), request);
        CompletableFuture<AbstractServerMessage> result = new CompletableFuture<>();
        openRequests.put(request.getId(), result::complete);

        try {
            String json = JSON_MAPPER.writeValueAsString(request);
            client.send(json);
        }
        catch (JsonProcessingException e) {
            return CompletableFuture.failedFuture(e);
        }

        return result;
    }
}
