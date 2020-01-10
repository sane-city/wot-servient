package city.sane.wot.binding.websocket;

import city.sane.wot.Servient;
import city.sane.wot.binding.ProtocolServer;
import city.sane.wot.binding.websocket.message.AbstractClientMessage;
import city.sane.wot.binding.websocket.message.AbstractServerMessage;
import city.sane.wot.thing.ExposedThing;
import city.sane.wot.thing.action.ExposedThingAction;
import city.sane.wot.thing.event.ExposedThingEvent;
import city.sane.wot.thing.form.Form;
import city.sane.wot.thing.form.Operation;
import city.sane.wot.thing.property.ExposedThingProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.typesafe.config.Config;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class WebsocketProtocolServer implements ProtocolServer {
    private static final ObjectMapper JSON_MAPPER = new ObjectMapper();

    private final Logger log = LoggerFactory.getLogger(WebsocketProtocolServer.class);
    private final ServerBootstrap serverBootstrap;
    private final EventLoopGroup serverBossGroup;
    private final EventLoopGroup serverWorkerGroup;
    private Map<String, ExposedThing> things;
    private List<String> addresses;
    private int bindPort;
    private Channel serverChannel;

    public WebsocketProtocolServer(Config config) {
        bindPort = config.getInt("wot.servient.websocket.bind-port");

        serverBossGroup = new NioEventLoopGroup(1);
        serverWorkerGroup = new NioEventLoopGroup();
        serverBootstrap = new ServerBootstrap();
        serverBootstrap.group(serverBossGroup, serverWorkerGroup)
                .channel(NioServerSocketChannel.class)
//                .handler(new LoggingHandler(LogLevel.INFO))
                .childHandler(new WebSocketServerInitializer());

        if (!config.getStringList("wot.servient.websocket.addresses").isEmpty()) {
            addresses = config.getStringList("wot.servient.websocket.addresses");
        }
        else {
            addresses = Servient.getAddresses().stream().map(a -> "ws://" + a + ":" + bindPort + "").collect(Collectors.toList());
        }
        things = new HashMap<>();
    }

    @Override
    public CompletableFuture<Void> start() {
        if (serverChannel != null) {
            return CompletableFuture.completedFuture(null);
        }

        return CompletableFuture.runAsync(() -> {
            try {
                serverChannel = serverBootstrap.bind(bindPort).sync().channel();
            }
            catch (InterruptedException e) {
                log.warn("Start failed", e);
                throw new CompletionException(e);
            }
        });
    }

    @Override
    public CompletableFuture<Void> stop() {
        if (serverChannel == null) {
            return CompletableFuture.completedFuture(null);
        }

        return CompletableFuture.runAsync(() -> {
            try {
                serverChannel.close().sync();
                serverBossGroup.shutdownGracefully().sync();
                serverWorkerGroup.shutdownGracefully().sync();
                serverChannel = null;
            }
            catch (InterruptedException e) {
                log.warn("Stop failed", e);
                throw new CompletionException(e);
            }
        });
    }

    @Override
    public CompletableFuture<Void> expose(ExposedThing thing) {
        log.info("WebsocketServer exposes '{}'", thing.getTitle());
        things.put(thing.getId(), thing);

        for (String address : addresses) {
            exposeProperties(thing, address);
            exposeActions(thing, address);
            exposeEvents(thing, address);
        }

        return CompletableFuture.completedFuture(null);
    }

    @Override
    public CompletableFuture<Void> destroy(ExposedThing thing) {
        log.info("WebsocketServer stop exposing '{}'", thing.getTitle());
        things.remove(thing.getId());

        return CompletableFuture.completedFuture(null);
    }

    private void exposeProperties(ExposedThing thing, String address) {
//        Form allForm = new Form.Builder()
//                .setHref(address)
//                .setOp(Operation.READ_ALL_PROPERTIES, Operation.READ_MULTIPLE_PROPERTIES)
//                .build();
//        thing.addForm(allForm);
//        log.info("Assign '{}' for reading all properties", address);

        Map<String, ExposedThingProperty> properties = thing.getProperties();
        properties.forEach((name, property) -> {
            if (!property.isWriteOnly()) {
                property.addForm(new Form.Builder()
                        .setHref(address)
                        .setOp(Operation.READ_PROPERTY)
                        .setOptional("websocket:message", Map.of(
                                "type", "ReadProperty",
                                "thingId", thing.getId(),
                                "name", name
                        ))
                        .build());
                log.info("Assign '{}' to Property '{}'", address, name);
            }
            if (!property.isReadOnly()) {
                property.addForm(new Form.Builder()
                        .setHref(address)
                        .setOp(Operation.WRITE_PROPERTY)
                        .setOptional("websocket:message", Map.of(
                                "type", "WriteProperty",
                                "thingId", thing.getId(),
                                "name", name
                        ))
                        .build());
                log.info("Assign '{}' to Property '{}'", address, name);
            }

            // if property is observable add an additional form with a observable href
//            if (property.isObservable()) {
//                Form.Builder observableForm = new Form.Builder();
//                observableForm.setHref(address);
//                observableForm.setContentType(contentType);
//                observableForm.setOp(Operation.OBSERVE_PROPERTY);
//
//                property.addForm(observableForm.build());
//                log.info("Assign '{}' to observable Property '{}'", address, name);
//            }
        });
    }

    private void exposeActions(ExposedThing thing, String address) {
        Map<String, ExposedThingAction> actions = thing.getActions();
        actions.forEach((name, action) -> {
            action.addForm(new Form.Builder()
                    .setHref(address)
                    .setOp(Operation.INVOKE_ACTION)
                    .setOptional("websocket:message", Map.of(
                            "type", "InvokeAction",
                            "thingId", thing.getId(),
                            "name", name
                    ))
                    .build());
            log.info("Assign '{}' to Action '{}'", address, name);
        });
    }

    private void exposeEvents(ExposedThing thing, String address) {
        Map<String, ExposedThingEvent> events = thing.getEvents();
        events.forEach((name, event) -> {
            Form.Builder form = new Form.Builder();
            form.setHref(address);
            form.setOp(Operation.SUBSCRIBE_EVENT);

            event.addForm(form.build());
            log.info("Assign '{}' to Event '{}'", address, name);
        });
    }

    class ServientWebsocketServer extends WebSocketServer {
        ServientWebsocketServer(InetSocketAddress inetSocketAddress) {
            super(inetSocketAddress);
        }

        @Override
        public void onOpen(WebSocket conn, ClientHandshake handshake) {
            log.debug("New Websocket connection has been opened");
        }

        @Override
        public void onClose(WebSocket conn, int code, String reason, boolean remote) {
            log.debug("WebsocketServer is closing");
        }

        @Override
        public void onMessage(WebSocket conn, String json) {
            log.info("Received message: {}", json);

            Consumer<AbstractServerMessage> replyConsumer = m -> {
                try {
                    String outputJson = JSON_MAPPER.writeValueAsString(m);
                    log.info("Send message: {}", outputJson);
                    conn.send(outputJson);
                }
                catch (JsonProcessingException ex) {
                    log.warn("Unable to send message back to client", ex);
                }
            };

            try {
                AbstractClientMessage message = JSON_MAPPER.readValue(json, AbstractClientMessage.class);
                log.debug("Deserialized message to: {}", message);

                message.reply(replyConsumer, things);
            }
            catch (IOException e) {
                log.warn("Error on deserialization of message: {}", json);
            }
        }

        @Override
        public void onError(WebSocket conn, Exception ex) {
            log.warn("An error occured on websocket server", ex);
        }

        @Override
        public void onStart() {
            log.debug("WebsocketServer has been started");
        }
    }

    private class WebSocketServerInitializer extends ChannelInitializer<SocketChannel> {
        @Override
        protected void initChannel(SocketChannel ch) throws Exception {
            ChannelPipeline pipeline = ch.pipeline();
            pipeline.addLast(new HttpServerCodec());
            pipeline.addLast(new HttpObjectAggregator(65536));
            pipeline.addLast(new WebSocketServerCompressionHandler());
            pipeline.addLast(new WebSocketServerProtocolHandler("/", null, true));
            pipeline.addLast(new WebSocketFrameHandler());
        }

        private class WebSocketFrameHandler extends SimpleChannelInboundHandler<WebSocketFrame> {
            @Override
            protected void channelRead0(ChannelHandlerContext ctx, WebSocketFrame frame) throws Exception {
                if (frame instanceof TextWebSocketFrame) {
                    // Send the uppercase string back.
                    String json = ((TextWebSocketFrame) frame).text();

                    log.info("Received message: {}", json);

                    Consumer<AbstractServerMessage> replyConsumer = m -> {
                        try {
                            String outputJson = JSON_MAPPER.writeValueAsString(m);
                            log.info("Send message: {}", outputJson);
                            ctx.channel().writeAndFlush(new TextWebSocketFrame(outputJson));
                        }
                        catch (JsonProcessingException ex) {
                            log.warn("Unable to send message back to client", ex);
                        }
                    };

                    try {
                        AbstractClientMessage message = JSON_MAPPER.readValue(json, AbstractClientMessage.class);
                        log.debug("Deserialized message to: {}", message);

                        message.reply(replyConsumer, things);
                    }
                    catch (IOException e) {
                        log.warn("Error on deserialization of message: {}", json);
                    }
                }
                else {
                    String message = "unsupported frame type: " + frame.getClass().getName();
                    throw new UnsupportedOperationException(message);
                }
            }
        }
    }
}
