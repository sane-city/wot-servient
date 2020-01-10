package city.sane.wot.binding.websocket;

import city.sane.wot.Servient;
import city.sane.wot.binding.ProtocolServer;
import city.sane.wot.binding.handler.codec.JsonDecoder;
import city.sane.wot.binding.handler.codec.JsonEncoder;
import city.sane.wot.binding.websocket.codec.TextWebSocketFrameDecoder;
import city.sane.wot.binding.websocket.codec.TextWebSocketFrameEncoder;
import city.sane.wot.binding.websocket.message.AbstractClientMessage;
import city.sane.wot.binding.websocket.message.AbstractServerMessage;
import city.sane.wot.thing.ExposedThing;
import city.sane.wot.thing.action.ExposedThingAction;
import city.sane.wot.thing.event.ExposedThingEvent;
import city.sane.wot.thing.form.Form;
import city.sane.wot.thing.form.Operation;
import city.sane.wot.thing.property.ExposedThingProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
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
                .childHandler(new WebsocketProtocolServerInitializer());

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
            event.addForm(new Form.Builder()
                    .setHref(address)
                    .setOp(Operation.SUBSCRIBE_EVENT)
                    .setOptional("websocket:message", Map.of(
                            "type", "SubscribeEvent",
                            "thingId", thing.getId(),
                            "name", name
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
                protected void channelRead0(ChannelHandlerContext ctx, AbstractClientMessage message) throws Exception {
                    Consumer<AbstractServerMessage> replyConsumer = ctx.channel()::writeAndFlush;
                    message.reply(replyConsumer, things);
                }
            });
        }

    }

}
