package city.sane.wot.binding.websocket;

import city.sane.wot.binding.ProtocolClient;
import city.sane.wot.binding.ProtocolClientException;
import city.sane.wot.content.Content;
import city.sane.wot.thing.form.Form;
import city.sane.wot.thing.observer.Observer;
import city.sane.wot.thing.observer.Subscription;
import com.typesafe.config.Config;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.CompletableFuture;

public class WebsocketProtocolClient implements ProtocolClient {
    final static Logger log = LoggerFactory.getLogger(WebsocketProtocolClient.class);

    public WebsocketProtocolClient(Config config) throws ProtocolClientException {
    }

    @Override
    public CompletableFuture<Content> readResource(Form form) {
        return null;
    }

    @Override
    public CompletableFuture<Content> writeResource(Form form, Content content) {
        return null;
    }

    // TODO CompletableFuture subscribeResource(Form form, Observer<Content> observer)
    public CompletableFuture<Subscription> subscribeResource(Form form, Observer<Content> observer) {
        return null;
    }
}
