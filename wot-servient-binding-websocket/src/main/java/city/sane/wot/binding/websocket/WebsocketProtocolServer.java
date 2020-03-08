package city.sane.wot.binding.websocket;

import city.sane.wot.Servient;
import city.sane.wot.binding.ProtocolServer;
import city.sane.wot.binding.handler.codec.JsonDecoder;
import city.sane.wot.binding.handler.codec.JsonEncoder;
import city.sane.wot.binding.websocket.handler.codec.TextWebSocketFrameDecoder;
import city.sane.wot.binding.websocket.handler.codec.TextWebSocketFrameEncoder;
import city.sane.wot.binding.websocket.message.AbstractClientMessage;
import city.sane.wot.binding.websocket.message.AbstractServerMessage;
import city.sane.wot.thing.ExposedThing;
import city.sane.wot.thing.action.ExposedThingAction;
import city.sane.wot.thing.event.ExposedThingEvent;
import city.sane.wot.thing.form.Form;
import city.sane.wot.thing.form.Operation;
import city.sane.wot.thing.property.ExposedThingProperty;
import com.typesafe.config.Config;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.codec.http.websocketx.WebSocketServerProtocolHandler;
import io.netty.handler.codec.http.websocketx.extensions.compression.WebSocketServerCompressionHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import static java.util.concurrent.CompletableFuture.completedFuture;
import static java.util.concurrent.CompletableFuture.runAsync;

public class WebsocketProtocolServer implements ProtocolServer {
    static final String WEBSOCKET_MESSAGE_THING_ID = "thingId";
    static final String WEBSOCKET_MESSAGE_TYPE = "type";
    static final String WEBSOCKET_MESSAGE_NAME = "name";
    private static final String WEBSOCKET_MESSAGE = "websocket:message";
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
                .childHandler(new WebsocketProtocolServerInitializer());

        if (!config.getStringList("wot.servient.websocket.addresses").isEmpty()) {
            addresses = config.getStringList("wot.servient.websocket.addresses");
        }
        else {
            addresses = Servient.getAddresses().stream().map(a -> "ws://" + a + ":" + bindPort + "").collect(Collectors.toList());
        }
        things = new HashMap<>();
    }

    WebsocketProtocolServer(ServerBootstrap serverBootstrap,
                            EventLoopGroup serverBossGroup,
                            EventLoopGroup serverWorkerGroup,
                            Map<String, ExposedThing> things,
                            List<String> addresses,
                            int bindPort,
                            Channel serverChannel) {
        this.serverBootstrap = serverBootstrap;
        this.serverBossGroup = serverBossGroup;
        this.serverWorkerGroup = serverWorkerGroup;
        this.things = things;
        this.addresses = addresses;
        this.bindPort = bindPort;
        this.serverChannel = serverChannel;
    }

    @Override
    public CompletableFuture<Void> start(Servient servient) {
        if (serverChannel != null) {
            return completedFuture(null);
        }

        return runAsync(() -> serverChannel = serverBootstrap.bind(bindPort).syncUninterruptibly().channel());
    }

    @Override
    public CompletableFuture<Void> stop() {
        if (serverChannel == null) {
            return completedFuture(null);
        }

        return runAsync(() -> {
            serverChannel.close().syncUninterruptibly();
            serverBossGroup.shutdownGracefully().syncUninterruptibly();
            serverWorkerGroup.shutdownGracefully().syncUninterruptibly();
            serverChannel = null;
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

        return completedFuture(null);
    }

    @Override
    public CompletableFuture<Void> destroy(ExposedThing thing) {
        log.info("WebsocketServer stop exposing '{}'", thing.getTitle());
        things.remove(thing.getId());

        return completedFuture(null);
    }

    private void exposeProperties(ExposedThing thing, String address) {
        Map<String, ExposedThingProperty<Object>> properties = thing.getProperties();
        properties.forEach((name, property) -> {
            if (!property.isWriteOnly()) {
                property.addForm(new Form.Builder()
                        .setHref(address)
                        .setOp(Operation.READ_PROPERTY)
                        .setOptional(WEBSOCKET_MESSAGE, Map.of(
                                WEBSOCKET_MESSAGE_TYPE, "ReadProperty",
                                WEBSOCKET_MESSAGE_THING_ID, thing.getId(),
                                WEBSOCKET_MESSAGE_NAME, name
                        ))
                        .build());
                log.info("Assign '{}' to Property '{}'", address, name);
            }
            if (!property.isReadOnly()) {
                property.addForm(new Form.Builder()
                        .setHref(address)
                        .setOp(Operation.WRITE_PROPERTY)
                        .setOptional(WEBSOCKET_MESSAGE, Map.of(
                                WEBSOCKET_MESSAGE_TYPE, "WriteProperty",
                                WEBSOCKET_MESSAGE_THING_ID, thing.getId(),
                                WEBSOCKET_MESSAGE_NAME, name
                        ))
                        .build());
                log.info("Assign '{}' to Property '{}'", address, name);
            }

            // if property is observable add an additional form with a observable href
            if (property.isObservable()) {
                property.addForm(new Form.Builder()
                        .setHref(address)
                        .setOp(Operation.OBSERVE_PROPERTY)
                        .setOptional(WEBSOCKET_MESSAGE, Map.of(
                                WEBSOCKET_MESSAGE_TYPE, "SubscribeProperty",
                                WEBSOCKET_MESSAGE_THING_ID, thing.getId(),
                                WEBSOCKET_MESSAGE_NAME, name
                        ))
                        .build());
                log.info("Assign '{}' to observable Property '{}'", address, name);
            }
        });
    }

    private void exposeActions(ExposedThing thing, String address) {
        Map<String, ExposedThingAction<Object, Object>> actions = thing.getActions();
        actions.forEach((name, action) -> {
            action.addForm(new Form.Builder()
                    .setHref(address)
                    .setOp(Operation.INVOKE_ACTION)
                    .setOptional(WEBSOCKET_MESSAGE, Map.of(
                            WEBSOCKET_MESSAGE_TYPE, "InvokeAction",
                            WEBSOCKET_MESSAGE_THING_ID, thing.getId(),
                            WEBSOCKET_MESSAGE_NAME, name
                    ))
                    .build());
            log.info("Assign '{}' to Action '{}'", address, name);
        });
    }

    private void exposeEvents(ExposedThing thing, String address) {
        Map<String, ExposedThingEvent<Object>> events = thing.getEvents();
        events.forEach((name, event) -> {
            event.addForm(new Form.Builder()
                    .setHref(address)
                    .setOp(Operation.SUBSCRIBE_EVENT)
                    .setOptional(WEBSOCKET_MESSAGE, Map.of(
                            WEBSOCKET_MESSAGE_TYPE, "SubscribeEvent",
                            WEBSOCKET_MESSAGE_THING_ID, thing.getId(),
                            WEBSOCKET_MESSAGE_NAME, name
                    ))
                    .build());
            log.info("Assign '{}' to Event '{}'", address, name);
        });
    }

    private class WebsocketProtocolServerInitializer extends ChannelInitializer<SocketChannel> {
        @Override
        protected void initChannel(SocketChannel ch) throws Exception {
            ChannelPipeline pipeline = ch.pipeline();
            pipeline.addLast(new HttpServerCodec());
            pipeline.addLast(new HttpObjectAggregator(65536));
            pipeline.addLast(new WebSocketServerCompressionHandler());
            pipeline.addLast(new WebSocketServerProtocolHandler("/", null, true));

            pipeline.addLast(new TextWebSocketFrameDecoder());
            pipeline.addLast(new TextWebSocketFrameEncoder());

            pipeline.addLast(new JsonDecoder<>(AbstractClientMessage.class));
            pipeline.addLast(new JsonEncoder<>(AbstractServerMessage.class));

            pipeline.addLast(new SimpleChannelInboundHandler<AbstractClientMessage>() {
                @Override
                protected void channelRead0(ChannelHandlerContext ctx,
                                            AbstractClientMessage message) throws Exception {
                    Consumer<AbstractServerMessage> replyConsumer = ctx.channel()::writeAndFlush;
                    message.reply(replyConsumer, things);
                }
            });
        }
    }
}
