package city.sane.wot.binding.websocket.handler.codec;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandler;
import io.netty.handler.codec.MessageToMessageDecoder;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class TextWebSocketFrameDecoder extends MessageToMessageDecoder<TextWebSocketFrame> implements ChannelInboundHandler {
    private final Logger log = LoggerFactory.getLogger(TextWebSocketFrameDecoder.class);

    @Override
    protected void decode(ChannelHandlerContext ctx, TextWebSocketFrame frame, List<Object> out) {
        String text = ((TextWebSocketFrame) frame).text();
        log.info("Received text: {}", text);
        out.add(text);
    }
}
