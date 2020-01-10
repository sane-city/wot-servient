package city.sane.wot.binding.websocket;

import city.sane.wot.binding.ProtocolClient;
import city.sane.wot.binding.ProtocolClientException;
import city.sane.wot.binding.ProtocolClientNotImplementedException;
import city.sane.wot.binding.websocket.message.*;
import city.sane.wot.content.Content;
import city.sane.wot.thing.form.Form;
import city.sane.wot.thing.observer.Observer;
import city.sane.wot.thing.observer.Subscription;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.http.DefaultHttpHeaders;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpClientCodec;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.websocketx.*;
import io.netty.util.CharsetUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
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
        return CompletableFuture.supplyAsync(() -> {
            try {
                WebsocketClient client = new WebsocketClient(form);
                return client;
            }
            catch (URISyntaxException | InterruptedException e) {
                throw new CompletionException(e);
            }
        });
    }

    private CompletableFuture<AbstractServerMessage> ask(WebsocketClient client, AbstractClientMessage request) {
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

        try {
            String json = JSON_MAPPER.writeValueAsString(request);
            client.send(json);
        }
        catch (JsonProcessingException e) {
            throw new ProtocolClientException(e);
        }

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

        public WebsocketClient(Form form) throws URISyntaxException, InterruptedException {
            uri = new URI(form.getHref());
            WebsocketClientHandler handler = new WebsocketClientHandler(
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
                                    handler);
                        }
                    });
            channel = clientBootstrap.connect(uri.getHost(), uri.getPort()).sync().channel();
            handler.handshakeFuture().sync();
        }

        public void close() {
            channel.close().syncUninterruptibly();
            group.shutdownGracefully().syncUninterruptibly();
        }

        public URI getURI() {
            return uri;
        }

        public void send(String message) {
            WebSocketFrame frame = new TextWebSocketFrame(message);
            channel.writeAndFlush(frame);
        }

        private class WebsocketClientHandler extends SimpleChannelInboundHandler<Object> {
            private final WebSocketClientHandshaker handshaker;
            private ChannelPromise handshakeFuture;

            public WebsocketClientHandler(WebSocketClientHandshaker handshaker) {
                this.handshaker = handshaker;
            }

            public ChannelFuture handshakeFuture() {
                return handshakeFuture;
            }

            @Override
            public void handlerAdded(ChannelHandlerContext ctx) {
                handshakeFuture = ctx.newPromise();
            }

            @Override
            public void channelActive(ChannelHandlerContext ctx) {
                handshaker.handshake(ctx.channel());
            }

            @Override
            public void channelInactive(ChannelHandlerContext ctx) {
//                System.out.println("WebSocket Client disconnected!");
            }

            @Override
            public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
                cause.printStackTrace();
                if (!handshakeFuture.isDone()) {
                    handshakeFuture.setFailure(cause);
                }
                ctx.close();
            }

            @Override
            public void channelRead0(ChannelHandlerContext ctx, Object msg) throws Exception {
                Channel ch = ctx.channel();
                if (!handshaker.isHandshakeComplete()) {
                    try {
                        handshaker.finishHandshake(ch, (FullHttpResponse) msg);
//                        System.out.println("WebSocket Client connected!");
                        handshakeFuture.setSuccess();
                    }
                    catch (WebSocketHandshakeException e) {
//                        System.out.println("WebSocket Client failed to connect");
                        handshakeFuture.setFailure(e);
                    }
                    return;
                }

                if (msg instanceof FullHttpResponse) {
                    FullHttpResponse response = (FullHttpResponse) msg;
                    throw new IllegalStateException(
                            "Unexpected FullHttpResponse (getStatus=" + response.getStatus() +
                                    ", content=" + response.content().toString(CharsetUtil.UTF_8) + ')');
                }

                WebSocketFrame frame = (WebSocketFrame) msg;
                if (frame instanceof TextWebSocketFrame) {
                    String json = ((TextWebSocketFrame) frame).text();

                    log.debug("Received message on websocket client for socket '{}': {}", uri, json);
                    AbstractServerMessage message = JSON_MAPPER.readValue(json, AbstractServerMessage.class);

                    if (message != null) {
                        Consumer<AbstractServerMessage> openRequest = openRequests.get(message.getId());

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
                    else {
                        log.warn("Message is null. Discard!");
                    }
                }
                else if (frame instanceof PongWebSocketFrame) {
//                    System.out.println("WebSocket Client received pong");
                }
                else if (frame instanceof CloseWebSocketFrame) {
//                    System.out.println("WebSocket Client received closing");
                    ch.close();
                }
            }
        }
    }
}
