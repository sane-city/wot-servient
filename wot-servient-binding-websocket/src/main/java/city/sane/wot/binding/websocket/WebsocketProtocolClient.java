package city.sane.wot.binding.websocket;

import city.sane.wot.binding.ProtocolClient;
import city.sane.wot.binding.ProtocolClientException;
import city.sane.wot.binding.websocket.message.AbstractMessage;
import city.sane.wot.binding.websocket.message.ReadPropertyResponse;
import city.sane.wot.binding.websocket.message.SubscribePropertyResponse;
import city.sane.wot.binding.websocket.message.WritePropertyResponse;
import city.sane.wot.content.Content;
import city.sane.wot.thing.form.Form;
import city.sane.wot.thing.observer.Observer;
import city.sane.wot.thing.observer.Subject;
import city.sane.wot.thing.observer.Subscription;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.typesafe.config.Config;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.concurrent.CompletableFuture;

public class WebsocketProtocolClient implements ProtocolClient {
    private final static Logger log = LoggerFactory.getLogger(WebsocketProtocolClient.class);
    private final static ObjectMapper JSON_MAPPER = new ObjectMapper();
    private final WebSocketClient cc;
    private Subject<Content> newSubject;


    WebsocketProtocolClient(Config config) throws ProtocolClientException, URISyntaxException {
        cc = new WebSocketClient(new URI("ws://localhost:8080")) {
            @Override
            public void onOpen(ServerHandshake serverHandshake) {
                cc.connect();
                log.info("onOpen status=" + serverHandshake.getHttpStatus() + ", statusMsg=" + serverHandshake.getHttpStatusMessage());
            }

            @Override
            public void onMessage(String json) {
                log.info("onMessage message= " + json);
                try {
                    AbstractMessage message = JSON_MAPPER.readValue(json, AbstractMessage.class);
                    if (message instanceof ReadPropertyResponse) {
                        System.out.println("ReadPropertyResponse");
                    } else if (message instanceof WritePropertyResponse) {
                        System.out.println("WritePropertyResponse");
                    } else if (message instanceof SubscribePropertyResponse) {
                        System.out.println("SubscribePropertyResponse");
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
    }

    @Override
    public CompletableFuture<Content> readResource(Form form) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                String json = JSON_MAPPER.writeValueAsString(form);
                cc.send(json);
            } catch (JsonProcessingException e) {
                e.printStackTrace();
            }
            // TODO
            return null;
        });
    }

    @Override
    public CompletableFuture<Content> writeResource(Form form, Content content) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                Form writeForm = new Form.Builder(form).setOptional("payload", content).build();
                String json = JSON_MAPPER.writeValueAsString(writeForm);
                cc.send(json);
            } catch (JsonProcessingException e) {
                e.printStackTrace();
            }
            // TODO
            return null;
        });
    }

    // TODO CompletableFuture subscribeResource(Form form, Observer<Content> observer)
    public CompletableFuture<Subscription> subscribeResource(Form form, Observer<Content> observer) {
        String topic;
        try {
            topic = new URI(form.getHref()).getPath().substring(1);
        } catch (URISyntaxException e) {
            log.warn("Unable to subscribe resource: {}", e.getMessage());
        }
        newSubject = new Subject<>();
        CompletableFuture<Subscription> result = new CompletableFuture<>();
        Subscription subscription = newSubject.subscribe(observer);
        return CompletableFuture.runAsync(() -> {
            try {
                Form subscribeForm = new Form.Builder(form).setOptional("observer", observer).build();
                String json = JSON_MAPPER.writeValueAsString(subscribeForm);
                cc.send(json);
            } catch (JsonProcessingException e) {
                e.printStackTrace();
            }
        }).thenApply(done -> subscription);
    }
}
