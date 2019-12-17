package city.sane.wot.binding.websocket;

import city.sane.wot.binding.ProtocolClient;
import city.sane.wot.binding.ProtocolClientException;
import city.sane.wot.binding.websocket.message.AbstractServerMessage;
import city.sane.wot.binding.websocket.message.ReadPropertyResponse;
import city.sane.wot.binding.websocket.message.WritePropertyResponse;
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
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.ExecutionException;

public class WebsocketProtocolClient implements ProtocolClient {
    private final static Logger log = LoggerFactory.getLogger(WebsocketProtocolClient.class);
    private static ObjectMapper JSON_MAPPER = new ObjectMapper();
    private final Map<URI, WebSocketClient> clients;
    CompletableFuture<Content> future = new CompletableFuture<>();

    public WebsocketProtocolClient(Map<URI, WebSocketClient> clients) {
        this.clients = clients;
    }

    @Override
    public CompletableFuture<Content> readResource(Form form) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                String json = JSON_MAPPER.writeValueAsString(form);
                // TODO: need return value for test
                getClientFor(form).send(json);
                return future.get();
            } catch (JsonProcessingException | InterruptedException | ExecutionException | ProtocolClientException e) {
                throw new CompletionException(e);
            }
        });
    }

    // FIXME: create websocket clients in factory
    private synchronized WebSocketClient getClientFor(Form form) throws ProtocolClientException {
        try {
            URI uri = new URI(form.getHref());
            WebSocketClient client = clients.get(uri);
            if (client == null) {
                log.info("Create ne websocket client for '{}'", uri);
                client = new WebSocketClient(uri) {
                    @Override
                    public void onOpen(ServerHandshake serverHandshake) {
                        log.info("onOpen status=" + serverHandshake.getHttpStatus() + ", statusMsg=" + serverHandshake.getHttpStatusMessage());
                    }

                    @Override
                    public void onMessage(String json) {
                        log.info("onMessage message= " + json);
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

                    }

                    @Override
                    public void onError(Exception e) {
                        log.info(e.getMessage());
                    }
                };
                client.connect();

                clients.put(uri, client);
            }

            return client;
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
                getClientFor(form).send(json);

                // TODO:
                return null;
            } catch (JsonProcessingException | ProtocolClientException e) {
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
