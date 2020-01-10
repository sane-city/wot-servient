package city.sane.wot.binding.websocket.handler.codec;

import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageEncoder;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;

import java.util.List;

public class TextWebSocketFrameEncoder extends MessageToMessageEncoder<String> {
    @Override
    protected void encode(ChannelHandlerContext ctx, String text, List<Object> out) {
        TextWebSocketFrame frame = new TextWebSocketFrame(text);
        out.add(frame);
    }
}
