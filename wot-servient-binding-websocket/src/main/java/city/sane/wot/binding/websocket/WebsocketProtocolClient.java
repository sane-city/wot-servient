package city.sane.wot.binding.websocket;

import city.sane.wot.binding.ProtocolClient;
import city.sane.wot.binding.ProtocolClientException;
import city.sane.wot.binding.ProtocolClientNotImplementedException;
import city.sane.wot.binding.handler.codec.JsonDecoder;
import city.sane.wot.binding.handler.codec.JsonEncoder;
import city.sane.wot.binding.websocket.handler.WebsocketClientHandshakerHandler;
import city.sane.wot.binding.websocket.handler.codec.TextWebSocketFrameDecoder;
import city.sane.wot.binding.websocket.handler.codec.TextWebSocketFrameEncoder;
import city.sane.wot.binding.websocket.message.*;
import city.sane.wot.content.Content;
import city.sane.wot.thing.form.Form;
import city.sane.wot.thing.observer.Observer;
import city.sane.wot.thing.observer.Subscription;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.http.DefaultHttpHeaders;
import io.netty.handler.codec.http.HttpClientCodec;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.websocketx.CloseWebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketClientHandshakerFactory;
import io.netty.handler.codec.http.websocketx.WebSocketVersion;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.function.Consumer;

/**
 * TODO: Currently a WebsocketProtocolClient and therefore also a WebsocketClient is created for each Thing. Even if each thing is reachable via the same socket. It would be better if there was only one WebsocketClient per socket and that is shared by all WebsocketProtocolClient instanceis.
 * TODO: WebsocketClient.close() is never called!
 */
public class WebsocketProtocolClient implements ProtocolClient {
    private final static Logger log = LoggerFactory.getLogger(WebsocketProtocolClient.class);

    private static ObjectMapper JSON_MAPPER = new ObjectMapper();
    private final Map<URI, WebsocketClient> clients = new HashMap<>();
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

    @Override
    public CompletableFuture<Subscription> subscribeResource(Form form, Observer<Content> observer) throws ProtocolClientNotImplementedException {
        Object message = form.getOptional("websocket:message");
        if (message != null) {
            try {
                AbstractClientMessage clientMessage = JSON_MAPPER.convertValue(message, AbstractClientMessage.class);

                try {
                    WebsocketClient client = getClientFor(form).get();
                    Subscription response = observe(client, clientMessage, observer);
                    return CompletableFuture.completedFuture(response);
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

    private CompletableFuture<Content> sendMessage(Form form) {
        Object message = form.getOptional("websocket:message");
        if (message != null) {
            try {
                AbstractClientMessage clientMessage = JSON_MAPPER.convertValue(message, AbstractClientMessage.class);

                try {
                    WebsocketClient client = getClientFor(form).get();
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
                    WebsocketClient client = getClientFor(form).get();
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

    private synchronized CompletableFuture<WebsocketClient> getClientFor(Form form) throws ProtocolClientException {
        try {
            URI uri = new URI(form.getHref());
            WebsocketClient client = clients.get(uri);
            if (client == null || !client.isOpen()) {
                log.info("Create new websocket client for socket '{}'", uri);
                CompletableFuture<WebsocketClient> result = new CompletableFuture<>();

                try {
                    client = new WebsocketClient(uri);
                    clients.put(uri, client);
                    result.complete(client);
                }
                catch (InterruptedException e) {
                    result.completeExceptionally(e);
                }

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


    private CompletableFuture<AbstractServerMessage> ask(WebsocketClient client, AbstractClientMessage request) {
        log.debug("Websocket client for socket '{}' is sending message: {}", client.getURI(), request);
        CompletableFuture<AbstractServerMessage> result = new CompletableFuture<>();
        openRequests.put(request.getId(), result::complete);

        client.send(request);

        return result;
    }

    private Subscription observe(WebsocketClient client, AbstractClientMessage request, Observer<Content> observer) throws ProtocolClientException {
        log.debug("Websocket client for socket '{}' is sending message: {}", client.getURI(), request);
        openRequests.put(request.getId(), m -> {
            if (m instanceof SubscribeNextResponse) {
                observer.next(m.toContent());
            }
            else if (m instanceof SubscribeCompleteResponse) {
                observer.complete();
            }
            else if (m instanceof SubscribeErrorResponse) {
                observer.error(((SubscribeErrorResponse) m).getError());
            }
        });

        client.send(request);

        // TODO: inform server to stop?
        return new Subscription(() -> openRequests.remove(request.getId()));
    }

    public void destroy() {
        clients.values().forEach(WebsocketClient::close);
    }

    private class WebsocketClient {
        private final Channel channel;
        private final EventLoopGroup group;
        private final URI uri;
        private final WebsocketClientHandshakerHandler handler;

        public WebsocketClient(URI uri) throws InterruptedException {
            this.uri = uri;
            handler = new WebsocketClientHandshakerHandler(
                    WebSocketClientHandshakerFactory.newHandshaker(uri, WebSocketVersion.V13, null, false, new DefaultHttpHeaders())
            );
            group = new NioEventLoopGroup();
            Bootstrap clientBootstrap = new Bootstrap();
            clientBootstrap.group(group)
                    .channel(NioSocketChannel.class)
                    .handler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel ch) {
                            ChannelPipeline pipeline = ch.pipeline();
                            pipeline.addLast(
                                    new HttpClientCodec(),
                                    new HttpObjectAggregator(8192),
                                    new TextWebSocketFrameEncoder(),
                                    new JsonEncoder<>(AbstractClientMessage.class),
                                    handler,
                                    new TextWebSocketFrameDecoder(),
                                    new JsonDecoder<>(AbstractServerMessage.class),
                                    new SimpleChannelInboundHandler<AbstractServerMessage>() {
                                        @Override
                                        protected void channelRead0(ChannelHandlerContext channelHandlerContext,
                                                                    AbstractServerMessage message) {
                                            Consumer<AbstractServerMessage> openRequest = openRequests.get(message.getId());
                                            log.debug("Received message on websocket client for socket '{}': {}", uri, message);

                                            if (openRequest != null) {
                                                log.debug("Found open request. Accept");
                                                openRequest.accept(message);

                                                if (message instanceof FinalResponse) {
                                                    openRequests.remove(message.getId());
                                                }
                                            }
                                            else {
                                                log.warn("Unexpected response. Discard!");
                                            }
                                        }
                                    }
                            );
                        }
                    });
            channel = clientBootstrap.connect(uri.getHost(), uri.getPort()).sync().channel();
            handler.handshakeFuture().sync();
        }

        public void close() {
            channel.writeAndFlush(new CloseWebSocketFrame());
            channel.close().syncUninterruptibly();
            group.shutdownGracefully().syncUninterruptibly();
        }

        public URI getURI() {
            return uri;
        }

        public void send(AbstractClientMessage message) {
            channel.writeAndFlush(message);
        }

        public boolean isOpen() {
            return handler.handshakeFuture().isSuccess();
        }
    }
}
