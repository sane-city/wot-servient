package city.sane.wot.binding.websocket;

import city.sane.wot.binding.ProtocolClient;
import city.sane.wot.binding.ProtocolClientNotImplementedException;
import city.sane.wot.binding.websocket.message.*;
import city.sane.wot.content.Content;
import city.sane.wot.content.ContentCodecException;
import city.sane.wot.content.ContentManager;
import city.sane.wot.thing.form.Form;
import city.sane.wot.thing.form.Operation;
import city.sane.wot.thing.observer.Observer;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketServerProtocolHandler;
import io.netty.handler.codec.http.websocketx.extensions.compression.WebSocketServerCompressionHandler;
import org.java_websocket.WebSocket;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.server.WebSocketServer;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import static org.junit.Assert.assertEquals;

public class WebsocketProtocolClientIT {
    private static final ObjectMapper JSON_MAPPER = new ObjectMapper();

    private WebsocketProtocolClientFactory clientFactory;
    private ProtocolClient client;
    private NioEventLoopGroup serverBossGroup;
    private NioEventLoopGroup serverWorkerGroup;
    private ServerBootstrap serverBootstrap;
    private Channel serverChannel;

    @Before
    public void setUp() throws InterruptedException {
        clientFactory = new WebsocketProtocolClientFactory();
        clientFactory.init().join();

        client = clientFactory.getClient();

        serverBossGroup = new NioEventLoopGroup(1);
        serverWorkerGroup = new NioEventLoopGroup();
        serverBootstrap = new ServerBootstrap();
        serverBootstrap.group(serverBossGroup, serverWorkerGroup)
                .channel(NioServerSocketChannel.class)
//                .handler(new LoggingHandler(LogLevel.INFO))
                .childHandler(new WebSocketServerInitializer());
        serverChannel = serverBootstrap.bind(8081).sync().channel();
    }

    @After
    public void tearDown() throws InterruptedException {
        clientFactory.destroy().join();

        serverChannel.close().sync();
        serverBossGroup.shutdownGracefully().sync();
        serverWorkerGroup.shutdownGracefully().sync();
    }

    @Test(timeout = 20 * 1000L)
    public void readResource() throws ContentCodecException, ExecutionException, InterruptedException {
        Form form = new Form.Builder()
                .setHref("ws://localhost:8081")
                .setOp(Operation.READ_PROPERTY)
                .setOptional("websocket:message", Map.of(
                        "type", "ReadProperty",
                        "thingId", "counter",
                        "name", "count"
                ))
                .build();

        assertEquals(ContentManager.valueToContent(1337), client.readResource(form).get());
    }

    @Test(timeout = 20 * 1000L)
    public void writeResource() throws ContentCodecException, ExecutionException, InterruptedException {
        Form form = new Form.Builder()
                .setHref("ws://localhost:8081")
                .setOp(Operation.WRITE_PROPERTY)
                .setOptional("websocket:message", Map.of(
                        "type", "WriteProperty",
                        "thingId", "counter",
                        "name", "count"
                ))
                .build();

        assertEquals(Content.EMPTY_CONTENT, client.writeResource(form, ContentManager.valueToContent(1337)).get());
    }

    @Test(timeout = 20 * 1000L)
    public void invokeResource() throws ContentCodecException, ExecutionException, InterruptedException {
        Form form = new Form.Builder()
                .setHref("ws://localhost:8081")
                .setOp(Operation.INVOKE_ACTION)
                .setOptional("websocket:message", Map.of(
                        "type", "InvokeAction",
                        "thingId", "counter",
                        "name", "increment"
                ))
                .build();

        assertEquals(ContentManager.valueToContent(43), client.invokeResource(form).get());
    }

    @Test(timeout = 20 * 1000L)
    public void subscribeProperty() throws ContentCodecException, ExecutionException, InterruptedException, ProtocolClientNotImplementedException {
        Form form = new Form.Builder()
                .setHref("ws://localhost:8081")
                .setOp(Operation.INVOKE_ACTION)
                .setOptional("websocket:message", Map.of(
                        "type", "SubscribeProperty",
                        "thingId", "counter",
                        "name", "count"
                ))
                .build();

        CompletableFuture<Content> future = new CompletableFuture<>();
        Observer<Content> observer = new Observer<>(future::complete);
        client.subscribeResource(form, observer).get();

        assertEquals(ContentManager.valueToContent(9001), future.get());
    }

    @Test(timeout = 20 * 1000L)
    public void subscribeEvent() throws ContentCodecException, ExecutionException, InterruptedException, ProtocolClientNotImplementedException {
        Form form = new Form.Builder()
                .setHref("ws://localhost:8081")
                .setOp(Operation.INVOKE_ACTION)
                .setOptional("websocket:message", Map.of(
                        "type", "SubscribeEvent",
                        "thingId", "counter",
                        "name", "change"
                ))
                .build();

        CompletableFuture<Content> future = new CompletableFuture<>();
        Observer<Content> observer = new Observer<>(future::complete);
        client.subscribeResource(form, observer).get();

        assertEquals(Content.EMPTY_CONTENT, future.get());
    }

    private class WebSocketServerInitializer  extends ChannelInitializer<SocketChannel> {
        @Override
        protected void initChannel(SocketChannel ch) throws Exception {
            ChannelPipeline pipeline = ch.pipeline();
            pipeline.addLast(new HttpServerCodec());
            pipeline.addLast(new HttpObjectAggregator(65536));
            pipeline.addLast(new WebSocketServerCompressionHandler());
            pipeline.addLast(new WebSocketServerProtocolHandler("/", null, true));
            pipeline.addLast(new WebSocketServerInitializer.WebSocketFrameHandler());
        }

        private class WebSocketFrameHandler extends SimpleChannelInboundHandler<WebSocketFrame> {
            @Override
            protected void channelRead0(ChannelHandlerContext ctx, WebSocketFrame frame) throws Exception {
                if (frame instanceof TextWebSocketFrame) {
                    // Send the uppercase string back.
                    String requestJson = ((TextWebSocketFrame) frame).text();

                    try {
                        AbstractClientMessage request = JSON_MAPPER.readValue(requestJson, AbstractClientMessage.class);

                        AbstractServerMessage response = null;
                        if (request instanceof ReadProperty) {
                            response = new ReadPropertyResponse(request.getId(), ContentManager.valueToContent(1337));
                        }
                        else if (request instanceof WriteProperty) {
                            response = new WritePropertyResponse(request.getId(), Content.EMPTY_CONTENT);
                        }
                        else if (request instanceof InvokeAction) {
                            response = new InvokeActionResponse(request.getId(), ContentManager.valueToContent(43));
                        }
                        else if (request instanceof SubscribeProperty) {
                            response = new SubscribeNextResponse(request.getId(), ContentManager.valueToContent(9001));
                        }
                        else if (request instanceof SubscribeEvent) {
                            response = new SubscribeNextResponse(request.getId(), Content.EMPTY_CONTENT);
                        }
                        else {
                            throw new RuntimeException("Unknown request: " + request.toString());
                        }

                        String responseJson = JSON_MAPPER.writeValueAsString(response);
                        ctx.channel().writeAndFlush(new TextWebSocketFrame(responseJson));
                    }
                    catch (IOException | ContentCodecException e) {
                        throw new RuntimeException(e);
                    }
                }
                else {
                    String message = "unsupported frame type: " + frame.getClass().getName();
                    throw new UnsupportedOperationException(message);
                }
            }
        }
    }

    private static class MyWebSocketServer extends WebSocketServer {
        public MyWebSocketServer(InetSocketAddress address) {
            super(address);
        }

        @Override
        public void onOpen(WebSocket conn, ClientHandshake handshake) {

        }

        @Override
        public void onClose(WebSocket conn, int code, String reason, boolean remote) {

        }

        @Override
        public void onMessage(WebSocket conn, String requestJson) {
            try {
                AbstractClientMessage request = JSON_MAPPER.readValue(requestJson, AbstractClientMessage.class);

                AbstractServerMessage response = null;
                if (request instanceof ReadProperty) {
                    response = new ReadPropertyResponse(request.getId(), ContentManager.valueToContent(1337));
                }
                else if (request instanceof WriteProperty) {
                    response = new WritePropertyResponse(request.getId(), Content.EMPTY_CONTENT);
                }
                else if (request instanceof InvokeAction) {
                    response = new InvokeActionResponse(request.getId(), ContentManager.valueToContent(43));
                }
                else if (request instanceof SubscribeProperty) {
                    response = new SubscribeNextResponse(request.getId(), ContentManager.valueToContent(9001));
                }
                else if (request instanceof SubscribeEvent) {
                    response = new SubscribeNextResponse(request.getId(), Content.EMPTY_CONTENT);
                }
                else {
                    throw new RuntimeException("Unknown request: " + request.toString());
                }

                String responseJson = JSON_MAPPER.writeValueAsString(response);
                conn.send(responseJson);
            }
            catch (IOException | ContentCodecException e) {
                throw new RuntimeException(e);
            }
        }

        @Override
        public void onError(WebSocket conn, Exception ex) {
            throw new RuntimeException(ex);
        }

        @Override
        public void onStart() {

        }
    }
}